package bulk;

import com.github.ziplet.filter.compression.CompressingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import javax.servlet.Filter;
import java.util.Arrays;

@Configuration
public class Conf {

    @Bean
    public Filter compressingFilter() {
        return new CompressingFilter();
    }
}
