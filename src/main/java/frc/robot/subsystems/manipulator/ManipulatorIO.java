// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.manipulator;

import org.littletonrobotics.junction.AutoLog;

public interface ManipulatorIO {
  @AutoLog
  public static class ManipulatorIOInputs {
    public boolean isMotorConnected = false;

    public double appliedVoltage = 0.0;
    public double supplyCurrentAmps = 0.0;
    public double statorCurrentAmps = 0.0;
    public double temperatureCelsius = 0.0;

    public boolean beambreakBroken = false;
  }

  /**
   * Write data from the hardware to the inputs object
   * 
   * @param inputs The inputs object
   */
  public default void updateInputs(ManipulatorIOInputs inputs) {}

  /**
   * @param volts The voltage that should be applied to the motor from -12 to 12
   */
  public default void setVoltage(double volts) {}

  /** 
   * Commands the hardware to stop. When using TalonFX, this commands the
   * motors to a Neutral control
   */
  public default void stop() {}

  /**
   * Enables brake or coast on the motor, only on the real motors. Useful 
   * since we usually keep them on brake, but may want to set them to coast 
   * when disabled
   * 
   * @param enableBrake
   */
  public default void setBrakeMode(boolean enableBrake) {}
}