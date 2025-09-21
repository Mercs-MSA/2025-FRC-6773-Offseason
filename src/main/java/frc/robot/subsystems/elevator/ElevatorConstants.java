package frc.robot.subsystems.elevator;

import frc.robot.Constants;

import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;

public class ElevatorConstants {
    public record ElevatorHardware(
      int motorId,
      double gearing,
      double drumRadiusMeters,
      double drumCircumferenceMeters
    ) {}

    public record ElevatorGains(
    // Feedback control
    double p, 
    double i, 
    double d, 
    // Motion magic constraints
    double maxVelocityMetersPerSecond, 
    double maxAccelerationMetersPerSecondSquared, 
    double jerkMetersPerSecondCubed, 
    // Elevator feedforward values
    double s, 
    double v, 
    double a, 
    double g) {}

    // public record motorConfig
}