package frc.robot.subsystems.Elevator;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.measure.Voltage;
import frc.robot.subsystems.Elevator.ElevatorConstants.ElevatorGains;
import frc.robot.subsystems.Elevator.ElevatorConstants.ElevatorHardware;
import frc.robot.subsystems.Elevator.ElevatorConstants.ElevatorMotorConfiguration;

public class ElevatorIOTalonFX implements ElevatorIO {

    private final TalonFX kMotor;

    private TalonFXConfiguration motorConfiguration = new TalonFXConfiguration();

    private StatusSignal<Angle> positionRotations;
    private StatusSignal<AngularVelocity> velocityRotationsPerSec;
    private StatusSignal<Voltage> appliedVolts;
    private StatusSignal<Current> supplyCurrentAmps;
    private StatusSignal<Current> statorCurrentAmps;
    private StatusSignal<Temperature> temperatureCelsius;

    private final VoltageOut kVoltageControl = new VoltageOut(0.0);
    private final MotionMagicVoltage kPositionControl = new MotionMagicVoltage(0.0);

    private final double kDrumCircumferenceMeters;

    public ElevatorIOTalonFX(String canbus,
    ElevatorHardware hardware,
    ElevatorMotorConfiguration configuration,   
    ElevatorGains gains) 
    {
        kMotor = new TalonFX(hardware.motorId(), canbus);
        kDrumCircumferenceMeters = hardware.drumCircumferenceMeters();

        motorConfiguration.Slot0.kP = metersToRotations(gains.p());
        motorConfiguration.Slot0.kI = metersToRotations(gains.i());
        motorConfiguration.Slot0.kD = metersToRotations(gains.d());
        motorConfiguration.Slot0.kS = gains.s();
        motorConfiguration.Slot0.kV = metersToRotations(gains.v());
        motorConfiguration.Slot0.kA = metersToRotations(gains.a());
        motorConfiguration.Slot0.kG = gains.g();
        motorConfiguration.MotionMagic.MotionMagicAcceleration = metersToRotations(gains.maxAccelerationMetersPerSecondSquared());
        motorConfiguration.MotionMagic.MotionMagicJerk = metersToRotations(gains.jerkMetersPerSecondCubed());
        motorConfiguration.MotionMagic.MotionMagicCruiseVelocity = metersToRotations(gains.maxVelocityMetersPerSecond());

        motorConfiguration.CurrentLimits.SupplyCurrentLimit = configuration.supplyCurrentLimitAmps();
        motorConfiguration.CurrentLimits.SupplyCurrentLimitEnable = configuration.enableSupplyCurrentLimit();
        motorConfiguration.CurrentLimits.StatorCurrentLimit = configuration.statorCurrentLimitAmps();
        motorConfiguration.CurrentLimits.StatorCurrentLimitEnable = configuration.enableStatorCurrentLimit();

        motorConfiguration.MotorOutput.NeutralMode = configuration.neutralMode();
        motorConfiguration.MotorOutput.Inverted = configuration.invert() ? InvertedValue.CounterClockwise_Positive : InvertedValue.Clockwise_Positive;

        kMotor.setPosition(0);
        motorConfiguration.Feedback.SensorToMechanismRatio = hardware.gearing();

        motorConfiguration.Feedback.FeedbackSensorSource = FeedbackSensorSourceValue.RotorSensor;

        kMotor.getConfigurator().apply(motorConfiguration, 1.0);
        
        positionRotations = kMotor.getPosition();
        velocityRotationsPerSec = kMotor.getVelocity();
        appliedVolts = kMotor.getMotorVoltage();
        supplyCurrentAmps = kMotor.getSupplyCurrent();
        statorCurrentAmps = kMotor.getStatorCurrent();
        temperatureCelsius = kMotor.getDeviceTemp();

        BaseStatusSignal.setUpdateFrequencyForAll(ElevatorConstants.kStatusSignalUpdateFrequencyHz, positionRotations, velocityRotationsPerSec, appliedVolts, supplyCurrentAmps, statorCurrentAmps, temperatureCelsius);

        kMotor.optimizeBusUtilization(0.0, 1.0);

    }

    
    @Override
    public void updateInputs(ElevatorIOInputs inputs) {
        inputs.isMotorConnected = BaseStatusSignal.refreshAll(positionRotations, velocityRotationsPerSec, appliedVolts, supplyCurrentAmps, statorCurrentAmps, temperatureCelsius)
        .isOK();

        inputs.positionMeters = rotationsToMeters(positionRotations.getValueAsDouble());
        inputs.velocityMetersPerSec = rotationsToMeters(velocityRotationsPerSec.getValueAsDouble());
        inputs.appliedVolts = appliedVolts.getValueAsDouble();
        inputs.statorCurrentAmps = statorCurrentAmps.getValueAsDouble();
        inputs.supplyCurrentAmps = supplyCurrentAmps.getValueAsDouble();
        inputs.temperatureCelsius = temperatureCelsius.getValueAsDouble();
    }

    @Override
    public void setVoltage(double volts) {
        kMotor.setControl(kVoltageControl.withOutput(volts));
    }

    @Override
    public void setPosition(double positionMeters) {
        kMotor.setControl(kPositionControl.withPosition(rotationsToMeters(positionMeters)).withSlot(0));
    }

    @Override
    public void stop() {
        kMotor.stopMotor();
    }

    @Override
    public void resetPosition() {
        kMotor.setPosition(0.0);
    }

    @Override
    public void setGains(double p, double i, double d, double v, double s, double g, double a) {
        var slot0 = new Slot0Configs();
        slot0.kP = metersToRotations(p);
        slot0.kI = metersToRotations(i);
        slot0.kD = metersToRotations(d);
        slot0.kS = s;
        slot0.kV = metersToRotations(v);
        slot0.kG = g;

        kMotor.getConfigurator().apply(slot0);
    }

    @Override
    public void setMotionMagicConstraints(double maxVelocity, double maxAcceleration) {
        var motionMagic = motorConfiguration.MotionMagic;
        motionMagic.MotionMagicCruiseVelocity = metersToRotations(maxVelocity);
        motionMagic.MotionMagicAcceleration = metersToRotations(maxAcceleration);
        motionMagic.MotionMagicJerk = 10 * metersToRotations(maxAcceleration);

        kMotor.getConfigurator().apply(motionMagic);
    }

    @Override
    public void setBrakeMode(boolean brake) {
        kMotor.setNeutralMode(brake ? NeutralModeValue.Brake : NeutralModeValue.Coast);
    }

    private double rotationsToMeters(double rotations) {
        return (rotations * kDrumCircumferenceMeters) * 2.0;
    }

    private double metersToRotations(double meters) {
        /*
         * Divide by two since this is a two-stage cascading-elevator where both stages move
         * simultaneously. If this was a three-stage cascading-elevator we would divide by 3
         */
        return (meters / kDrumCircumferenceMeters) / 2.0;
    }


    
    
}
