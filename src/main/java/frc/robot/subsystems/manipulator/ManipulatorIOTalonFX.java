// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.manipulator;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.NeutralOut;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.subsystems.manipulator.ManipulatorConstants.ManipulatorHardware;
import frc.robot.subsystems.manipulator.ManipulatorConstants.ManipulatorTalonFXConfiguration;

public class ManipulatorIOTalonFX implements ManipulatorIO {
  private final TalonFX kMotorFlyWheel;
  private final DigitalInput kBeamBreak;

  private final TalonFXConfiguration motorConfiguration = new TalonFXConfiguration();

  // Motor data we wish to log
  private StatusSignal<AngularVelocity> velocityRotationsPerSecFly;
  private StatusSignal<Voltage> appliedVoltsFly;
  private StatusSignal<Current> supplyCurrentAmpsFly;
  private StatusSignal<Current> statorCurrentAmpsFly;
  private StatusSignal<Temperature> temperatureCelsiusFly;

  // Control mode
  private final VoltageOut kVoltageControl = new VoltageOut(0.0);

  public ManipulatorIOTalonFX(
      String canbus,
      ManipulatorHardware hardware,
      ManipulatorTalonFXConfiguration configuration,
      double statusSignalUpdateFrequency) {

    kMotorFlyWheel = new TalonFX(hardware.flyWheelMotorId(), canbus);
    kBeamBreak = new DigitalInput(hardware.beamBreakIO());

    motorConfiguration.MotorOutput.NeutralMode = configuration.neutralMode();
    motorConfiguration.MotorOutput.Inverted =
        configuration.invert()
            ? InvertedValue.CounterClockwise_Positive
            : InvertedValue.Clockwise_Positive;

    kMotorFlyWheel.getConfigurator().apply(motorConfiguration, 1.0);

    // Status signals
    velocityRotationsPerSecFly = kMotorFlyWheel.getVelocity();
    appliedVoltsFly = kMotorFlyWheel.getMotorVoltage();
    supplyCurrentAmpsFly = kMotorFlyWheel.getSupplyCurrent();
    statorCurrentAmpsFly = kMotorFlyWheel.getStatorCurrent();
    temperatureCelsiusFly = kMotorFlyWheel.getDeviceTemp();

    BaseStatusSignal.setUpdateFrequencyForAll(
        statusSignalUpdateFrequency,
        velocityRotationsPerSecFly,
        appliedVoltsFly,
        supplyCurrentAmpsFly,
        statorCurrentAmpsFly,
        temperatureCelsiusFly);

    // Optimize CAN bus utilization
    kMotorFlyWheel.optimizeBusUtilization();
  }

  public ManipulatorIOTalonFX(
      ManipulatorHardware hardware,
      ManipulatorTalonFXConfiguration configuration,
      double statusSignalUpdateFrequency) {
    this("rio", hardware, configuration, statusSignalUpdateFrequency);
  }

  @Override
  public void updateInputs(ManipulatorIOInputs inputs) {
    boolean ok = BaseStatusSignal.refreshAll(
            velocityRotationsPerSecFly,
            appliedVoltsFly,
            supplyCurrentAmpsFly,
            statorCurrentAmpsFly,
            temperatureCelsiusFly)
        .isOK();

    inputs.isMotorConnected = ok;
    inputs.flywheelVelocityRotPerSec = velocityRotationsPerSecFly.getValueAsDouble();
    inputs.flywheelAppliedVolts = appliedVoltsFly.getValueAsDouble();
    inputs.flywheelSupplyCurrentAmps = supplyCurrentAmpsFly.getValueAsDouble();
    inputs.flywheelStatorCurrentAmps = statorCurrentAmpsFly.getValueAsDouble();
    inputs.flywheelTemperatureCelsius = temperatureCelsiusFly.getValueAsDouble();
    inputs.beambreakBroken = kBeamBreak.get();
  }

  @Override
  public void setVoltage(double volts) {
    kMotorFlyWheel.setControl(kVoltageControl.withOutput(volts));
  }

  @Override
  public void stop() {
    kMotorFlyWheel.setControl(new NeutralOut());
  }

  @Override
  public void setBrakeMode(boolean enableBrake) {
    kMotorFlyWheel.setNeutralMode(enableBrake ? NeutralModeValue.Brake : NeutralModeValue.Coast);
  }
}