package info.quazi.valueProtect.converter;

import info.quazi.valueProtect.entity.Property.PropertyType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PropertyTypeConverter implements AttributeConverter<PropertyType, String> {

    @Override
    public String convertToDatabaseColumn(PropertyType propertyType) {
        if (propertyType == null) {
            return null;
        }
        return propertyType.getDisplayName();
    }

    @Override
    public PropertyType convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return PropertyType.fromDisplayName(dbValue);
    }
}