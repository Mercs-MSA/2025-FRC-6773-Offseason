// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.manipulator;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Manipulator extends SubsystemBase {

  private final ManipulatorIO kHardware;
  private final ManipulatorIOInputsAutoLogged kInputs = new ManipulatorIOInputsAutoLogged();
  
  public Manipulator(ManipulatorIO manipulatorIO) {
    kHardware = manipulatorIO;
  }

  @Override
  public void periodic() {
    kHardware.updateInputs(kInputs);
    Logger.processInputs("Manipulator/Inputs", kInputs);
  }

  public void stop() {
      kHardware.stop();
  }

  public void setVoltage(double voltage) {
    kHardware.setVoltage(voltage);
  }

  public boolean getCoralDetected() {
      return kInputs.beambreakBroken;
  }
}
