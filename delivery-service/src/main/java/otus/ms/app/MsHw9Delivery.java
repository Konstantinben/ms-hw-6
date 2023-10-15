package otus.ms.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsHw9Delivery {

    public static void main(String[] args) {
        SpringApplication.run(MsHw9Delivery.class, args);
    }

}
