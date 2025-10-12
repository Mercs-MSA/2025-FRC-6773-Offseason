package frc.robot.subsystems.elevator;

import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import frc.robot.Constants;

public class ElevatorConstants {
  public static final double kDrumRadiusMeters = Units.inchesToMeters(0.944);
  public static final double kDrumCircumferenceMeters = 2.0 * Math.PI * kDrumRadiusMeters;
  public static final double kMaxPositionMeters = Units.inchesToMeters(60.0);
  public static final double kMinPositionMeters = Units.inchesToMeters(-0.1);
  public static final double kPositionToleranceMeters = 0.01;

  /** The frequency that telemetry form the motor is pushed to the CANBus */
  public static final double kStatusSignalUpdateFrequencyHz = 100.0; 
  public static final int kLinearFilterSampleCount = 5;
  public static final int kAmpFilterThreshold = 40;
  public static final boolean kHomeWithCurrent = false;

  public record ElevatorHardware (
      int motorIdLeft,
      int motorIdRight, 
      double gearing, 
      double drumRadiusMeters, 
      double drumCircumferenceMeters) {}

  public record ElevatorGains (
      double p, 
      double i,
      double d,
      double v,
      double s,
      double g,
      double a,
      double maxVelocityMetersPerSecond,
      double maxAccelerationMetersPerSecondSquared,
      double jerkMetersPerSecondCubed
  ) {}

  public record ElevatorMotorConfiguration (
      boolean invert,
      boolean enableStatorCurrentLimit,
      boolean enableSupplyCurrentLimit,
      double statorCurrentLimitAmps,
      double supplyCurrentLimitAmps,
      double peakForwardVoltage,
      double peakReverseVoltage,
      NeutralModeValue neutralMode
  ) {}

  public record SimulationConfiguration(
      DCMotor motorType,
      double carriageMassKg,
      double drumRadiusMeters,
      boolean simulateGravity,
      double startingHeightMeters,
      double measurementStdDevs) {}

  public static final ElevatorHardware kRoboElevatorHardware = new ElevatorHardware(
    51,
    52, // Motor CAN ID
    9.0 / 1.0,  // Gearing
    /*
    * Outside sprocket radius: 0.944 in
    * Root sprocket radius: 0.819
    */
    kDrumRadiusMeters, // Drum (sprocket) radius
    kDrumCircumferenceMeters); // Drum (sprocket) circumference

  public static final ElevatorGains kElevatorGains = 
  switch (Constants.kCurrentMode) {
    case REAL -> new ElevatorGains(
      5.0,
      0.0,
      0.0,
      0.0, //2.947
      0, // 22
      0.0,
      0.0,
      0,
      0, 
      0); // 0.11
    case SIM -> new ElevatorGains(
      1.0,
      0.0,
      0.0,
      10.0,
      25.0,
      0.0,
      0.0,
      14.34,
      0.01,
      0.11);
    default -> new ElevatorGains(
      0.0,
      0.0,
      0.0,
      0.0,
      0.0,
      0.0,
      0.0,
      0.0,
      0.0,
      0.0);
  };

  public static final ElevatorMotorConfiguration kMotorConfiguration = new ElevatorMotorConfiguration(
    false, 
    true, 
    true, 
    80.0, 
    50.0, 
    12.0,
    -12.0,
    NeutralModeValue.Brake);

  public static final SimulationConfiguration kSimulationConfiguration = new SimulationConfiguration(
    DCMotor.getKrakenX60(1), 
    // empty carriage load = .8kg
    // prototype carriage load = 13.61 kg
    5.0, 
    kDrumRadiusMeters, 
    true, 
    0.0, 
    0.0002);
}
