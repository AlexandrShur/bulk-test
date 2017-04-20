package bulk;

import com.github.ziplet.filter.compression.CompressingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.servlet.Filter;

@Configuration
public class Conf {

    @Bean
    public Filter compressingFilter() {
        return new CompressingFilter();
    }
}
