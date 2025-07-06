package com.tuum.fsaccountsservice.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuum.common.domain.entities.Account;

import com.tuum.common.domain.entities.Balance;
import com.tuum.fsaccountsservice.dto.resonse.AccountResponse;
import com.tuum.fsaccountsservice.dto.resonse.BalanceResponse;
import lombok.extern.slf4j.Slf4j;


import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class DtoMapper {

    public static AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getAccountId(),
                account.getCustomerId(),
                account.getCountry(),
                toBalanceResponses(account.getBalances()),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
    public static BalanceResponse toBalanceResponse(Balance balance) {
        return new BalanceResponse(
                balance.getBalanceId(),
                balance.getAccountId(),
                balance.getCurrency(),
                balance.getAvailableAmount(),
                balance.getCreatedAt(),
                balance.getUpdatedAt()
        );
    }

    public static List<BalanceResponse> toBalanceResponses(List<Balance> balances) {
        return balances.stream()
                .map(DtoMapper::toBalanceResponse)
                .collect(Collectors.toList());
    }

} 