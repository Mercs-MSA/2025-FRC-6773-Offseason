// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.intake;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.littletonrobotics.junction.AutoLogOutput;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.utils.debugging.LoggedTunableNumber;
import frc.robot.utils.visualizers.PivotVisualizer;
import edu.wpi.first.math.filter.LinearFilter;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;

public class Intake extends SubsystemBase {
  public enum IntakePivotGoal {
    kFloorPickup(() -> Rotation2d.fromRotations(-0.1)),
    kStationPickup(() -> Rotation2d.fromRotations(-0.25)),
    kStow(() -> Rotation2d.fromDegrees(0.015)),
    /** Custom setpoint that can be modified over network tables; Useful for debugging */
    custom(() -> Rotation2d.fromDegrees(
      new LoggedTunableNumber("Intake/Feedback/PivotSetpointDegrees", 0.0).get()));

    private Supplier<Rotation2d> goalPosition;

    IntakePivotGoal(Supplier<Rotation2d> goalPosition) {
      this.goalPosition = goalPosition;
    }

    public Rotation2d getGoalPosition() {
      return this.goalPosition.get();
    }
  }

  private final IntakePivotIO kPivotHardware;
  private final IntakePivotIOInputsAutoLogged kPivotInputs = new IntakePivotIOInputsAutoLogged();

  private final IntakeRollerIO kRollerHardware;
  private final IntakeRollerIOInputsAutoLogged kRollerInputs = new IntakeRollerIOInputsAutoLogged();

  private final LoggedTunableNumber kP =
      new LoggedTunableNumber("Intake/Gains/Pivot_kP", IntakeConstants.kPivotGains.p());
  private final LoggedTunableNumber kI =
      new LoggedTunableNumber("Intake/Gains/Pivot_kI", IntakeConstants.kPivotGains.i());
  private final LoggedTunableNumber kD =
      new LoggedTunableNumber("Intake/Gains/Pivot_kD", IntakeConstants.kPivotGains.d());
  private final LoggedTunableNumber kS =
      new LoggedTunableNumber("Intake/Gains/Pivot_kS", IntakeConstants.kPivotGains.s());
  private final LoggedTunableNumber kV =
      new LoggedTunableNumber("Intake/Gains/Pivot_kV", IntakeConstants.kPivotGains.v());
  private final LoggedTunableNumber kA =
      new LoggedTunableNumber("Intake/Gains/Pivot_kA", IntakeConstants.kPivotGains.a());
  private final LoggedTunableNumber kG =
      new LoggedTunableNumber("Intake/Gains/Pivot_kG", IntakeConstants.kPivotGains.g());
  private final LoggedTunableNumber kMaxVelocity =
      new LoggedTunableNumber(
          "Intake/MotionMagic/Pivot_kMaxVelocity", 
          IntakeConstants.kPivotGains.maxVelocityRotationsPerSecond());
  private final LoggedTunableNumber kMaxAcceleration =
      new LoggedTunableNumber(
          "Intake/MotionMagic/Pivot_kMaxAcceleration", 
          IntakeConstants.kPivotGains.maxAccelerationRotationsPerSecondSquared());

  private final LoggedTunableNumber kRollerVoltage = new LoggedTunableNumber("Intake/Roller/RollerVoltage", IntakeConstants.kRollerIntakingVoltage);

  private boolean detectedGamepiece = false;
  private IntakePivotGoal currentPivotGoal;

  private final PivotVisualizer kPivotVisualizer;
  //TODO add roller visualizer

  
  /** Creates a new Intake. */
  public Intake(IntakePivotIO pivotHardwareIO, IntakeRollerIO rollerHardwareIO) {
    kPivotHardware = pivotHardwareIO;
    kRollerHardware = rollerHardwareIO;

    kPivotVisualizer = new PivotVisualizer(
      "Intake/PivotVisualizer", 
      IntakeConstants.kPivotVisualizerConfiguration, 
      4.0, 
      new Color8Bit(Color.kBlue));

    //TODO: roller visualizer
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run

    kPivotHardware.updateInputs(kPivotInputs);
    kRollerHardware.updateInputs(kRollerInputs);
    Logger.processInputs("Intake/Inputs/Pivot", kPivotInputs);
    Logger.processInputs("Intake/Inputs/Roller", kRollerInputs);

    if (currentPivotGoal != null) {
      setPivotPosition(currentPivotGoal.getGoalPosition());
      Logger.recordOutput("Intake/PivotGoalValue", currentPivotGoal.getGoalPosition());
      Logger.recordOutput("Intake/PivotGoal", currentPivotGoal);
    } else {
      Logger.recordOutput("Intake/PivotGoal", "NONE");
    }




    

    // Check if pivot is attempting to move beyond its limitations
    if (getPivotPosition().getDegrees() > IntakeConstants.kMaxPivotPosition.getDegrees() 
        && kPivotInputs.appliedVoltage > 0.0) {
      stop(false, true);
    } else if (getPivotPosition().getDegrees() < IntakeConstants.kMinPivotPosition.getDegrees() 
        && kPivotInputs.appliedVoltage < 0.0) {
      stop(false, true);
    } else {
      // Do nothing if limits are not reached
    }

    // This says that if the value is changed in the advantageScope tool,
    // Then we change the values in the code. Saves deploy time.
    // More found in prerequisites slide
    LoggedTunableNumber.ifChanged(
      hashCode(),
      () -> {
        kPivotHardware.setGains(
            kP.get(), kI.get(), kD.get(), kS.get(), kG.get(), kV.get(), kA.get());
        //TODO: tunable voltage
      },
      kP,
      kI,
      kD,
      kS,
      kV,
      kA,
      kG);
    LoggedTunableNumber.ifChanged(
        hashCode(),
        () -> {
          kPivotHardware.setMotionMagicConstraints(kMaxVelocity.get(), kMaxAcceleration.get());
        },
        kMaxVelocity,
        kMaxAcceleration);

    // The visualizer needs to be periodically fed the current position of the mechanism
    kPivotVisualizer.updatePosition(getPivotPosition().times(-1.0));
  }

  public void setPivotGoal(IntakePivotGoal desiredGoal) {
    currentPivotGoal = desiredGoal;
  }

  public void stop(boolean stopRollers, boolean stopPivot) {
    if (stopRollers) {
      kRollerHardware.stop();
    }
    if (stopPivot) {
      currentPivotGoal = null;
      kPivotHardware.stop();
    }
  }

  public void setPivotVoltage(double voltage) {
    kPivotHardware.setVoltage(voltage);
  }

  public void setPivotPosition(Rotation2d position) {
    kPivotHardware.setPosition(position);
  }

  @AutoLogOutput(key = "Pivot/Feedback/ErrorDegrees")
  public double getPivotErrorDegrees() {
    if (currentPivotGoal != null && getPivotPosition() != null) {
      return currentPivotGoal.getGoalPosition().getDegrees() - getPivotPosition().getDegrees();
    } else {
      return 0.0;
    }
  }

  @AutoLogOutput(key = "Pivot/Feedback/AtGoal")
  public boolean pivotAtGoal() {
    return Math.abs(getPivotErrorDegrees()) < IntakeConstants.kPivotPositionTolerance.getDegrees();
  }

  public Rotation2d getPivotPosition() {
    return kPivotInputs.position;
  }

  public void runRollers() {
    kRollerHardware.setVoltage(IntakeConstants.kRollerIntakingVoltage);
  }

  public void setRollerVoltage(double volts) {
    kRollerHardware.setVoltage(volts);
  }

}
