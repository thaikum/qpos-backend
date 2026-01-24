package org.example.qposbackend.Authorization.AuthorityFieldFilter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
public class AuthorityBasedFieldSerializer extends StdSerializer<Object> {

    @Autowired
    private JsonSerializer<Object> defaultSerializer;

    protected AuthorityBasedFieldSerializer() {
        this(null);
    }

    protected AuthorityBasedFieldSerializer(Class<Object> t) {
        super(t);
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (Objects.isNull(value)) {
            gen.writeNull();
        } else {
            defaultSerializer.serialize(value, gen, provider);
        }
        gen.writeEndObject();
    }

    private boolean hasRequiredAuthorities(String[] requiredAuthorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Set<String> userAuthorities = new HashSet<>();
            authentication.getAuthorities().forEach(authority -> userAuthorities.add(authority.getAuthority()));
            return userAuthorities.containsAll(Arrays.asList(requiredAuthorities));
        }
        return false;
    }
}
