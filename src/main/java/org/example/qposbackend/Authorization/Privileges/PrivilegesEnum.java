package org.example.qposbackend.Authorization.Privileges;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PrivilegesEnum {
    // ======================= inventory =============================
    VIEW_INVENTORY("VIEW INVENTORY RECORDS", "INVENTORY"),
    UPDATE_INVENTORY("UPDATE INVENTORY RECORD", "INVENTORY"),
    ADD_INVENTORY_ITEM("ADD INVENTORY ITEM", "INVENTORY"),
    DELETE_INVENTORY_ITEM("DELETE INVENTORY ITEM", "INVENTORY"),

    //===================== stock ====================================
    ADD_STOCK("ADD STOCK", "STOCK"),
    VIEW_STOCK("VIEW STOCK", "STOCK"),
    UPDATE_STOCK("UPDATE EXISTING STOCK", "STOCK"),
    VIEW_BUYING_PRICE("VIEW ITEM BUYING PRICE", "STOCK"),
    UPDATE_SELLING_PRICE("UPDATE ITEM SELLING PRICE", "STOCK"),
    SET_DISCOUNT("SET DISCOUNT ON ITEM", "STOCK"),

    //========================= sales =================================
    MAKE_SALE("MAKE SALE", "SALES"),
    CANCEL_SALE("CANCEL UNPAID SALE", "SALES"),
    UPDATE_SALE("UPDATE SALE ITEMS", "SALES"),
    VIEW_HISTORICAL_SALES("VIEW HISTORICAL SALES", "SALES"),
    HANDLE_RETURNED_GOODS("HANDLE RETURNED GOODS", "SALES"),
    APPLY_DISCOUNT("OFFER DISCOUNTS", "SALES"),
    CLOSE_DAY_BOOKS("CLOSE_DAY_BOOKS", "SALES"),

    //======================== ROLES ==================================
    VIEW_ROLES("VIEW USER GROUPS", "ROLES"),
    ADD_ROLE("ADD USER GROUP", "ROLES"),

    //======================== USERS =================================
    VIEW_USERS("VIEW USERS", "USERS"),
    ADD_USER("ADD USER", "USERS"),

    //======================== ACCOUNTING ============================
    CREATE_ACCOUNT("CREATE ACCOUNT", "ACCOUNTS"),
    UPDATE_ACCOUNT("UPDATE ACCOUNT", "ACCOUNTS"),
    DELETE_ACCOUNT("DELETE ACCOUNT", "ACCOUNTS"),
    VIEW_ACCOUNTS("VIEW ACCOUNTS", "ACCOUNTS"),

    //======================= EXPENSES =============================
    VIEW_EXPENSES("VIEW EXPENSES", "ACCOUNTS"),
    ADD_EXPENSES("ADD EXPENSES", "EXPENSES"),

    // ======================== TRANSACTIONS =========================
    POST_TRANSACTION("POST TRANSACTION", "TRANSACTIONS"),
    VERIFY_TRANSACTION("VERIFY TRANSACTION", "TRANSACTIONS"),
    VIEW_TRANSACTIONS("VIEW TRANSACTIONS", "TRANSACTIONS"),

    //================================= ADMIN PARAMETERS =====================
    UPDATE_ADMIN_PARAMETERS("UPDATE ADMIN PARAMETERS", "ADMIN_PARAMETERS"),
    VIEW_ADMIN_PARAMETERS("VIEW ADMIN PARAMETERS", "ADMIN_PARAMETERS"),

    //================================ REPORTS ==============================
    VIEW_PROFIT_AND_LOSS_REPORT("VIEW PROFIT AND LOSS REPORT", "REPORTS"),
    VIEW_ACCOUNT_STATEMENT_REPORT("VIEW ACCOUNT STATEMENT REPORT", "REPORTS"),
    VIEW_RESTOCKING_ESTIMATES("VIEW RESTOCKING ESTIMATES", "REPORTS");

    private final String displayName;
    private final String category;
}
