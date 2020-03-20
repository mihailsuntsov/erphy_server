package com.laniakea.util;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

@Component
public class JSONSerializer extends JsonSerializer<Date>{

    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException,JsonProcessingException {


        jgen.writeString(format.format(value));
    }
}