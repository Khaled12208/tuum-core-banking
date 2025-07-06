package com.tuum.common.util;

import com.tuum.common.types.Currency;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Currency.class)
public class CurrencyTypeHandler extends BaseTypeHandler<Currency> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Currency parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.name(), java.sql.Types.OTHER);
    }

    @Override
    public Currency getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : Currency.valueOf(value);
    }

    @Override
    public Currency getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : Currency.valueOf(value);
    }

    @Override
    public Currency getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : Currency.valueOf(value);
    }
    
} 