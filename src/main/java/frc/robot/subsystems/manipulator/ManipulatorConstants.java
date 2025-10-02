
package frc.robot.subsystems.manipulator;

import com.ctre.phoenix6.signals.NeutralModeValue;

public class  ManipulatorConstants {

  public record ManipulatorHardware(
    int motorId,
    int beamBreakIO) {}

  public record ManipulatorTalonFXConfiguration(
   boolean invert,
    NeutralModeValue neutralMode) {}

  /** The frequency that telemetry form the motor is pushed to the CANBus */
  public static final double kStatusSignalUpdateFrequencyHz = 100.0;

  public static final double kRollerIntakeVoltage = 4.0;
  public static final double kRollerOuttakeVoltage = -5.0;

  public static final ManipulatorHardware kManipulatorHardware = new ManipulatorHardware(
    43, // Motor CAN ID
    1 // Beakbreak IO
    ); 

  public static final ManipulatorTalonFXConfiguration kMotorConfiguration = new ManipulatorTalonFXConfiguration(
    false, // Invert
    NeutralModeValue.Brake); // Idle mode
}