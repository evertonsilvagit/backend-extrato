package br.com.everton.backendextrato.application.conta.usecase;

import br.com.everton.backendextrato.domain.conta.AccountBill;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class AccountBillOrderNormalizer {

    private AccountBillOrderNormalizer() {
    }

    static void normalize(
            List<AccountBill> bills,
            Long prioritizedBillId,
            Integer prioritizedOrder,
            Consumer<List<AccountBill>> persistChanges
    ) {
        List<AccountBill> orderedBills = new ArrayList<>(bills);

        if (prioritizedBillId != null && prioritizedOrder != null) {
            AccountBill prioritized = null;
            for (AccountBill existing : orderedBills) {
                if (prioritizedBillId.equals(existing.id())) {
                    prioritized = existing;
                    break;
                }
            }

            if (prioritized != null) {
                orderedBills.remove(prioritized);
                orderedBills.add(clampOrder(prioritizedOrder, orderedBills.size() + 1) - 1, prioritized);
            }
        }

        List<AccountBill> changedBills = new ArrayList<>();
        for (int index = 0; index < orderedBills.size(); index++) {
            int expectedOrder = index + 1;
            AccountBill existing = orderedBills.get(index);
            if (!Integer.valueOf(expectedOrder).equals(existing.sortOrder())) {
                changedBills.add(new AccountBill(
                        existing.id(),
                        existing.description(),
                        existing.amount(),
                        existing.paymentDay(),
                        existing.categoryId(),
                        existing.categoryName(),
                        existing.activeMonths(),
                        existing.ownerEmail(),
                        expectedOrder
                ));
            }
        }

        if (!changedBills.isEmpty()) {
            persistChanges.accept(changedBills);
        }
    }

    private static Integer clampOrder(Integer requestedOrder, int maxOrder) {
        return Math.max(1, Math.min(requestedOrder, maxOrder));
    }
}
