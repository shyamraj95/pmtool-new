package com.api.pmtool.config;

import java.io.IOException;
import java.util.Map;

import com.api.pmtool.entity.User;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class UserRoleMapSerializer extends JsonSerializer<Map<User, String>> {
    @Override
    public void serialize(Map<User, String> userRoles, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        for (Map.Entry<User, String> entry : userRoles.entrySet()) {
            // Serialize user ID or fullName instead of toString()
            gen.writeFieldName(entry.getValue()); 
            gen.writeString(entry.getKey().getFullName()); // or use getId() for IDs
        }
        gen.writeEndObject();
    }
}