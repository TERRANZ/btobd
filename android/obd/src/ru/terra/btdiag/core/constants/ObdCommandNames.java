package ru.terra.btdiag.core.constants;

import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.commands.SpeedObdCommand;
import pt.lighthouselabs.obd.commands.control.*;
import pt.lighthouselabs.obd.commands.engine.*;
import pt.lighthouselabs.obd.commands.fuel.FuelConsumptionRateObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelEconomyObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelLevelObdCommand;
import pt.lighthouselabs.obd.commands.pressure.BarometricPressureObdCommand;
import pt.lighthouselabs.obd.commands.pressure.FuelPressureObdCommand;
import pt.lighthouselabs.obd.commands.pressure.IntakeManifoldPressureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.AirIntakeTemperatureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.EngineCoolantTemperatureObdCommand;
import pt.lighthouselabs.obd.enums.AvailableCommandNames;

/**
 * Date: 26.12.14
 * Time: 13:11
 */
public class ObdCommandNames {
    public static Class<? extends ObdCommand> getCommand(AvailableCommandNames name) {
        switch (name) {
            case AIR_INTAKE_TEMP:
                return AirIntakeTemperatureObdCommand.class;
            case AMBIENT_AIR_TEMP:
                return AmbientAirTemperatureObdCommand.class;
            case ENGINE_COOLANT_TEMP:
                return EngineCoolantTemperatureObdCommand.class;
            case BAROMETRIC_PRESSURE:
                return BarometricPressureObdCommand.class;
            case FUEL_PRESSURE:
                return FuelPressureObdCommand.class;
            case INTAKE_MANIFOLD_PRESSURE:
                return IntakeManifoldPressureObdCommand.class;
            case ENGINE_LOAD:
                return EngineLoadObdCommand.class;
            case ENGINE_RUNTIME:
                return EngineRuntimeObdCommand.class;
            case ENGINE_RPM:
                return EngineRPMObdCommand.class;
            case SPEED:
                return SpeedObdCommand.class;
            case MAF:
                return MassAirFlowObdCommand.class;
            case THROTTLE_POS:
                return ThrottlePositionObdCommand.class;
            case TROUBLE_CODES:
                return TroubleCodesObdCommand.class;
            case FUEL_LEVEL:
                return FuelLevelObdCommand.class;
            case FUEL_CONSUMPTION:
                return FuelConsumptionRateObdCommand.class;
            case FUEL_ECONOMY:
                return FuelEconomyObdCommand.class;
            case TIMING_ADVANCE:
                return TimingAdvanceObdCommand.class;
            case DTC_NUMBER:
                return DtcNumberObdCommand.class;
            case EQUIV_RATIO:
                return CommandEquivRatioObdCommand.class;
            case DISTANCE_TRAVELED_AFTER_CODES_CLEARED:
                return DistanceTraveledSinceCodesClearedObdCommand.class;
        }
        return null;
    }
}
