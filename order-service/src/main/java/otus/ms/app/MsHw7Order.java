package otus.ms.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsHw7Order {

    public static void main(String[] args) {
        SpringApplication.run(MsHw7Order.class, args);
    }

}
