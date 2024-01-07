package com.example.goran.warehousemanagement;

import android.provider.BaseColumns;

public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static class DeltaProducts implements BaseColumns {
        public static final String TABLE_NAME = "delta_products";
        public static final String MANUFACTURE = "manufacturer_name";
        public static final String MODEL = "model_name";
        public static final String PRICE = "price";
        public static final String QUANTITY = "quantity";
        public static final String SIZE = "size";
        public static final String OPERATION = "op";
        public static final String WAREHOUSE = "warehouse";
        public static final int NUM_COLUMNS = 7;
    }
    public static class DeltaProductsTemp implements BaseColumns {
        public static final String TABLE_NAME = "delta_products_temp";
        public static final String MANUFACTURE = "manufacturer_name";
        public static final String MODEL = "model_name";
        public static final String PRICE = "price";
        public static final String QUANTITY = "quantity";
        public static final String SIZE = "size";
        public static final String OPERATION = "op";
        public static final String WAREHOUSE = "warehouse";
        public static final int NUM_COLUMNS = 7;
    }

    public static class Products implements BaseColumns {
        public static final String TABLE_NAME = "products";
        public static final String MANUFACTURE = "manufacturer_name";
        public static final String MODEL = "model_name";
        public static final String PRICE = "price";
        public static final String QUANTITY = "quantity";
        public static final int NUM_COLUMNS = 4;
    }

    public static class GdasnkProducts implements BaseColumns {
        public static final String TABLE_NAME = "gdansk_products";
        public static final String MANUFACTURE = "manufacturer_name";
        public static final String MODEL = "model_name";
        public static final String PRICE = "price";
        public static final String QUANTITY = "quantity";
        public static final String SIZE = "size";
        public static final int NUM_COLUMNS = 5;
    }
    public static class WarsawProducts implements BaseColumns {
        public static final String TABLE_NAME = "warsaw_products";
        public static final String MANUFACTURE = "manufacturer_name";
        public static final String MODEL = "model_name";
        public static final String PRICE = "price";
        public static final String QUANTITY = "quantity";
        public static final String SIZE = "size";
        public static final int NUM_COLUMNS = 5;
    }
}