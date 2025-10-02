// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.manipulator;

import java.util.function.BiConsumer;

import org.littletonrobotics.junction.AutoLogOutput;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.NeutralOut;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.AsynchronousInterrupt;
import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.subsystems.manipulator.ManipulatorConstants.ManipulatorHardware;
import frc.robot.subsystems.manipulator.ManipulatorConstants.ManipulatorTalonFXConfiguration;

public class ManipulatorIOTalonFX implements ManipulatorIO {
  private final TalonFX kMotor;
  private final DigitalInput kBeamBreak;

  private TalonFXConfiguration motorConfiguration = new TalonFXConfiguration();

  // Motor data we wish to log
  private StatusSignal<AngularVelocity> velocityRotationsPerSec;
  private StatusSignal<Voltage> appliedVolts;
  private StatusSignal<Current> supplyCurrentAmps;
  private StatusSignal<Current> statorCurrentAmps;
  private StatusSignal<Temperature> temperatureCelsius;

  // Control modes
  private final VoltageOut kVoltageControl = new VoltageOut(0.0);

  public ManipulatorIOTalonFX(
    String canbus,
    ManipulatorHardware hardware,
    ManipulatorTalonFXConfiguration configuration,
    double statusSignalUpdateFrequency) {

    kMotor = new TalonFX(hardware.motorId(), canbus);

    kBeamBreak = new DigitalInput(hardware.beamBreakIO());

    motorConfiguration.MotorOutput.NeutralMode = configuration.neutralMode();
    motorConfiguration.MotorOutput.Inverted = 
      configuration.invert() 
        ? InvertedValue.CounterClockwise_Positive 
        : InvertedValue.Clockwise_Positive;
    kMotor.getConfigurator().apply(motorConfiguration, 1.0);
        
    // Get status signals from the motor controller
    velocityRotationsPerSec = kMotor.getVelocity();
    appliedVolts = kMotor.getMotorVoltage();
    supplyCurrentAmps = kMotor.getSupplyCurrent();
    statorCurrentAmps = kMotor.getStatorCurrent();
    temperatureCelsius = kMotor.getDeviceTemp();

    BaseStatusSignal.setUpdateFrequencyForAll(statusSignalUpdateFrequency,
      velocityRotationsPerSec,
      appliedVolts,
      supplyCurrentAmps,
      supplyCurrentAmps,
      statorCurrentAmps,
      temperatureCelsius);

    // Optimize the CANBus utilization by explicitly telling all CAN signals we
    // are not using to simply not be sent over the CANBus
    kMotor.optimizeBusUtilization(0.0, 1.0);
  }

  public ManipulatorIOTalonFX(
    ManipulatorHardware hardware,
    ManipulatorTalonFXConfiguration configuration,
    double statusSignalUpdateFrequency) {

    // Assumes the rio is the CANBus
    this("rio", hardware, configuration, statusSignalUpdateFrequency);
  }

  @Override
  public void updateInputs(ManipulatorIOInputs inputs) {
    inputs.isMotorConnected = BaseStatusSignal.refreshAll(
      velocityRotationsPerSec,
      appliedVolts,
      supplyCurrentAmps,
      supplyCurrentAmps,
      statorCurrentAmps,
      temperatureCelsius).isOK();

    inputs.appliedVoltage = appliedVolts.getValueAsDouble();
    inputs.supplyCurrentAmps = supplyCurrentAmps.getValueAsDouble();
    inputs.statorCurrentAmps = statorCurrentAmps.getValueAsDouble();
    inputs.temperatureCelsius = temperatureCelsius.getValueAsDouble();
    
    inputs.beambreakBroken = kBeamBreak.get();
  }

  @Override
  public void setVoltage(double volts) {
    kMotor.setControl(kVoltageControl.withOutput(volts));
  }

  @Override
  public void stop() {
    kMotor.setControl(new NeutralOut());
  }

  @Override
  public void setBrakeMode(boolean enableBrake) {
    kMotor.setNeutralMode(enableBrake ? NeutralModeValue.Brake : NeutralModeValue.Coast);
  }
}