package frc.robot.subsystems.manipulator;

import com.ctre.phoenix6.signals.NeutralModeValue;

public class ManipulatorConstants {

  public record ManipulatorHardware(
      int flyWheelMotorId,
      int beamBreakIO) {}

  public record ManipulatorTalonFXConfiguration(
      boolean invert,
      NeutralModeValue neutralMode) {}

  public static final double kStatusSignalUpdateFrequencyHz = 100.0;

  public static final ManipulatorHardware kManipulatorHardware =
      new ManipulatorHardware(61, 0);

  public static final ManipulatorTalonFXConfiguration kMotorConfiguration =
      new ManipulatorTalonFXConfiguration(false, NeutralModeValue.Brake);
}