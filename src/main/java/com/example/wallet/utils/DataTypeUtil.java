package com.example.wallet.utils;

public class DataTypeUtil {
  public static Integer parseInt(String value) {
    if (value != null) {
      return Integer.parseInt(value);
    } else {
      return Integer.valueOf(0);
    }
  }

  public static Long parseLong(String value) {
    if (value != null) {
      return Long.parseLong(value);
    } else {
      return Long.valueOf(0);
    }
  }
}
