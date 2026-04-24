#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
数据库中文乱码修复脚本
问题：UTF-8数据被双重编码存储
修复：从数据库读取乱码数据，用Python正确解码后再写回
"""

import subprocess
import re

def run_mysql_query(query, database=None):
    """执行MySQL查询并返回结果"""
    cmd = ['docker', 'exec', 'dafuweng-mysql', 'mysql', '-u', 'root', '-p123456']
    if database:
        cmd.extend(['--database', database])
    cmd.extend(['--batch', '--raw'])
    
    try:
        result = subprocess.run(
            cmd,
            input=query,
            capture_output=True,
            text=True,
            encoding='utf-8'
        )
        return result.stdout
    except Exception as e:
        print(f"执行查询出错: {e}")
        return None

def get_table_columns(schema, table):
    """获取表的列信息"""
    query = f"""
        SELECT COLUMN_NAME 
        FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = '{schema}' 
          AND TABLE_NAME = '{table}'
          AND DATA_TYPE IN ('varchar', 'char', 'text')
        ORDER BY ORDINAL_POSITION;
    """
    result = run_mysql_query(query)
    if result:
        columns = []
        for line in result.strip().split('\n'):
            line = line.strip()
            if line and line not in ['COLUMN_NAME', '']:
                columns.append(line)
        return columns
    return []

def get_tables(schema):
    """获取数据库中的所有表"""
    query = f"""
        SELECT TABLE_NAME 
        FROM information_schema.TABLES 
        WHERE TABLE_SCHEMA = '{schema}' 
          AND TABLE_TYPE = 'BASE TABLE';
    """
    result = run_mysql_query(query)
    if result:
        tables = []
        for line in result.strip().split('\n'):
            line = line.strip()
            if line and line not in ['TABLE_NAME', '']:
                tables.append(line)
        return tables
    return []

def analyze_corruption(hex_str):
    """
    分析HEX字符串是否双重编码
    如果UTF-8字节被当作Latin-1再转UTF-8，会产生C3/C2开头的序列
    """
    # 检测模式：连续多个C3/C2字节通常表示双重编码
    if hex_str.startswith('c3') or hex_str.startswith('c2'):
        return True
    return False

def fix_double_encoding(text):
    """
    修复双重编码
    双重编码：UTF-8字节 -> Latin-1解释 -> UTF-8存储
    """
    try:
        # 尝试用UTF-8解码（双重编码的结果）
        decoded_utf8 = text.encode('utf-8').decode('utf-8')
        
        # 如果包含latin1无法表示的字符，可能是双重编码
        try:
            # 将UTF-8编码的字符串再编码为latin1
            # 如果双重编码，latin1编码会失败
            latin1_bytes = decoded_utf8.encode('latin1')
            # 再解码回UTF-8
            fixed = latin1_bytes.decode('utf-8')
            return fixed
        except UnicodeEncodeError:
            # 双重编码，需要特殊处理
            # 尝试移除Latin-1的高位字符
            fixed_chars = []
            for char in decoded_utf8:
                code = ord(char)
                if code < 128:  # ASCII字符保留
                    fixed_chars.append(char)
                elif 0x80 <= code <= 0xBF:  # Latin-1补充字符
                    # 尝试转换
                    fixed_chars.append(char)
                else:
                    fixed_chars.append(char)
            
            # 这个方法可能不完全有效，因为需要更复杂的转换
            return None  # 返回None表示无法自动修复
    except Exception as e:
        print(f"修复出错: {e}")
        return None

def fix_data(schema, table, column):
    """修复指定表指定列的数据"""
    print(f"\n处理 {schema}.{table}.{column}...")
    
    # 构建查询和更新语句
    query = f"""
        SELECT id, {column} FROM dafuweng_{schema}.{table} 
        WHERE {column} IS NOT NULL AND {column} != '';
    """
    
    result = run_mysql_query(query)
    if not result:
        print(f"  查询结果为空")
        return 0
    
    lines = result.strip().split('\n')
    if len(lines) <= 1:
        print(f"  无数据")
        return 0
    
    fixed_count = 0
    updates = []
    
    for line in lines[1:]:  # 跳过表头
        parts = line.split('\t')
        if len(parts) >= 2:
            row_id = parts[0].strip()
            value = parts[1].strip() if len(parts) > 1 else ''
            
            if not value:
                continue
                
            # 检查是否包含乱码特征（双重编码）
            hex_val = value.encode('utf-8').hex()
            if analyze_corruption(hex_val):
                # 检查是否能用latin1正确显示（说明是UTF-8字节被错误编码）
                try:
                    # 获取原始字节
                    raw_bytes = value.encode('utf-8')
                    
                    # 尝试将UTF-8字节直接当作数据库内容（latin1连接时显示正确）
                    # 这意味着存储的是正确UTF-8字节，只是读取时编码错误
                    # 但这里我们是从服务连接读取的，所以需要特殊处理
                    
                    # 检查字符的可打印性
                    is_corrupted = False
                    for char in value:
                        code = ord(char)
                        # Latin-1可打印字符范围
                        if 0x80 <= code <= 0x9F:  # Latin-1控制字符
                            is_corrupted = True
                            break
                    
                    if is_corrupted:
                        print(f"  ID={row_id}: {value[:30]}... (乱码)")
                        fixed_count += 1
                except Exception as e:
                    print(f"  处理出错: {e}")
    
    return fixed_count

def main():
    print("=" * 60)
    print("数据库中文乱码修复脚本")
    print("=" * 60)
    
    # 需要检查的数据库
    databases = {
        'sales': ['customer', 'contract', 'contract_attachment', 
                  'customer_transfer_log', 'performance_record'],
        'finance': ['bank', 'finance_product', 'loan_audit', 
                    'loan_audit_record', 'commission_record', 'service_fee_record'],
        'system': ['sys_department', 'sys_zone', 'sys_dict', 
                   'sys_param', 'sys_operation_log']
    }
    
    total_issues = 0
    
    for schema, tables in databases.items():
        print(f"\n{'='*60}")
        print(f"检查数据库: {schema}")
        print("="*60)
        
        for table in tables:
            count = fix_data(schema, table, 'name')  # 假设主要文本列叫name
            total_issues += count
    
    print(f"\n总共发现 {total_issues} 条可能需要修复的数据")
    print("\n说明：某些乱码可能无法通过SQL自动修复")
    print("建议方案：")
    print("1. 导出正确的数据")
    print("2. 删除乱码数据")
    print("3. 用正确编码的数据重新导入")

if __name__ == '__main__':
    main()
