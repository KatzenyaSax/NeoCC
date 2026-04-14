package com.dafuweng.sales.task;

import com.dafuweng.common.entity.Result;
import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.sales.feign.SystemFeignClient;
import com.dafuweng.sales.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class PublicSeaTask {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SystemFeignClient systemFeignClient;

    /**
     * 每天凌晨2点扫描公海客户
     * 规则：status NOT IN (3,4,5) AND next_follow_up_date < now AND created_at < now - publicSeaDays
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scanPublicSeaCustomers() {
        Integer publicSeaDays = getPublicSeaDays();
        List<CustomerEntity> toPublicSea = customerService.listCustomerToPublicSea(publicSeaDays);

        for (CustomerEntity customer : toPublicSea) {
            customer.setStatus((short) 5);
            customer.setPublicSeaTime(new Date());
            customerService.update(customer);
        }
    }

    private Integer getPublicSeaDays() {
        try {
            Result<String> result = systemFeignClient.getParamValue("customer.public_sea_days");
            if (result != null && result.getData() != null) {
                return Integer.parseInt(result.getData());
            }
            return 30;
        } catch (Exception e) {
            return 30;
        }
    }
}