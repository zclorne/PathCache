package com.scu.zclorne;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceUtilsTest {

    @Test
    void getDistance() {
        System.out.println(DistanceUtils.getDistance(117.362083, 40.479588, 117.375, 40.478943));
        System.out.println(DistanceUtils.getDistance(117.362083, 40.479588, 117.25, 40.487274));
        System.out.println(DistanceUtils.getDistance(117.375, 40.421978, 117.372124, 40.416668));
        System.out.println(DistanceUtils.getDistance(117.180534, 40.493298, 117.182213, 40.492847));
    }
}