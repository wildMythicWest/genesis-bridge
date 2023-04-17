package com.genesis.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.genesis.common.domain.CommonModel;
import com.genesis.common.domain.DataType;
import com.genesis.common.domain.InputDataWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Component
@Slf4j
public class DataMapper {

    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Bean
    public Function<Message<byte[]>, CommonModel> convertToCommonModel() {
        return (message) -> {
            InputDataWrapper value;
            try {
                value = jsonMapper.readValue(new String(message.getPayload(), StandardCharsets.UTF_8), InputDataWrapper.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            log.info("Received {}", value);
            CommonModel commonModel;
            if (DataType.XML_V1.equals(value.getType())) {
                try {
                    JsonNode node = xmlMapper.readTree(value.getData().getBytes());
                    commonModel = CommonModel.builder()
                            .name(node.path("name").asText())
                            .age(node.path("age").asInt())
                            .build();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Can't convert data type.");
            }

            log.info("Sending {}", commonModel);
            return commonModel;
        };
    }
}
