package com.api.tinyfarm.repository;

public interface Stats {
    Long getUid();
    String getName();

    Double getProduction();
    Double getCapacity();

    Double getEcus();
}
