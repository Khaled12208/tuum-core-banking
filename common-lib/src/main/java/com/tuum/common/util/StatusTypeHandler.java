package com.tuum.common.util;

import com.tuum.common.types.TransactionStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(TransactionStatus.class)
public class StatusTypeHandler extends BaseTypeHandler<TransactionStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, TransactionStatus parameter, JdbcType jdbcType) throws SQLException {
        // For PostgreSQL enum, we need to pass the enum value as a string
        // The JDBC driver will handle the conversion to the enum type
        ps.setObject(i, parameter.name(), java.sql.Types.OTHER);
    }

    @Override
    public TransactionStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : TransactionStatus.valueOf(value);
    }

    @Override
    public TransactionStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : TransactionStatus.valueOf(value);
    }

    @Override
    public TransactionStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : TransactionStatus.valueOf(value);
    }
} 