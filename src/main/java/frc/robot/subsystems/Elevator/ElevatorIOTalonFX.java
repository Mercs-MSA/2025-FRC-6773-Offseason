package frc.robot.subsystems.Elevator;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
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

    private final TalonFX kMotorLeft;
    private final TalonFX kMotorRight;

    private TalonFXConfiguration motorConfiguration = new TalonFXConfiguration();

    private StatusSignal<Angle> positionRotationsLeft;
    private StatusSignal<AngularVelocity> velocityRotationsPerSecLeft;
    private StatusSignal<Voltage> appliedVoltsLeft;
    private StatusSignal<Current> supplyCurrentAmpsLeft;
    private StatusSignal<Current> statorCurrentAmpsLeft;
    private StatusSignal<Temperature> temperatureCelsiusLeft;

    private StatusSignal<Angle> positionRotationsRight;
    private StatusSignal<AngularVelocity> velocityRotationsPerSecRight;
    private StatusSignal<Voltage> appliedVoltsRight;
    private StatusSignal<Current> supplyCurrentAmpsRight;
    private StatusSignal<Current> statorCurrentAmpsRight;
    private StatusSignal<Temperature> temperatureCelsiusRight;

    private final VoltageOut kVoltageControl = new VoltageOut(0.0);
    private final MotionMagicVoltage kPositionControl = new MotionMagicVoltage(0.0);

    private final double kDrumCircumferenceMeters;

    public ElevatorIOTalonFX(
        ElevatorHardware hardware,
        ElevatorMotorConfiguration configuration,   
        ElevatorGains gains) 
    {
        kMotorLeft = new TalonFX(hardware.motorIdLeft());
        kMotorRight = new TalonFX(hardware.motorIdRight());
        
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

        kMotorLeft.setPosition(0);
        kMotorRight.setPosition(0);

        motorConfiguration.Feedback.SensorToMechanismRatio = hardware.gearing();

        motorConfiguration.Feedback.FeedbackSensorSource = FeedbackSensorSourceValue.RotorSensor;

        kMotorLeft.getConfigurator().apply(motorConfiguration, 1.0);
        kMotorRight.getConfigurator().apply(motorConfiguration, 1.0);

        positionRotationsLeft = kMotorLeft.getPosition();
        velocityRotationsPerSecLeft = kMotorLeft.getVelocity();
        appliedVoltsLeft = kMotorLeft.getMotorVoltage();
        supplyCurrentAmpsLeft = kMotorLeft.getSupplyCurrent();
        statorCurrentAmpsLeft = kMotorLeft.getStatorCurrent();
        temperatureCelsiusLeft = kMotorLeft.getDeviceTemp();

        BaseStatusSignal.setUpdateFrequencyForAll(ElevatorConstants.kStatusSignalUpdateFrequencyHz, positionRotationsLeft, velocityRotationsPerSecLeft, appliedVoltsLeft, supplyCurrentAmpsLeft, statorCurrentAmpsLeft, temperatureCelsiusLeft);

        kMotorLeft.optimizeBusUtilization(0.0, 1.0);
        kMotorRight.optimizeBusUtilization(0.0, 1.0);
        
        kMotorRight.setControl(new Follower(hardware.motorIdLeft(), true));
    }

    
    @Override
    public void updateInputs(ElevatorIOInputs inputs) {
        inputs.isMotorConnected = BaseStatusSignal.refreshAll(positionRotationsLeft, velocityRotationsPerSecLeft, appliedVoltsLeft, supplyCurrentAmpsLeft, statorCurrentAmpsLeft, temperatureCelsiusLeft)
        .isOK();

        inputs.positionMeters = rotationsToMeters(positionRotationsLeft.getValueAsDouble());
        inputs.velocityMetersPerSec = rotationsToMeters(velocityRotationsPerSecLeft.getValueAsDouble());
        inputs.appliedVolts = appliedVoltsLeft.getValueAsDouble();
        inputs.statorCurrentAmps = statorCurrentAmpsLeft.getValueAsDouble();
        inputs.supplyCurrentAmps = supplyCurrentAmpsLeft.getValueAsDouble();
        inputs.temperatureCelsius = temperatureCelsiusLeft.getValueAsDouble();
    }

    @Override
    public void setVoltage(double volts) {
        kMotorLeft.setControl(kVoltageControl.withOutput(volts));
    }

    @Override
    public void setPosition(double positionMeters) {
        kMotorLeft.setControl(kPositionControl.withPosition(rotationsToMeters(positionMeters)).withSlot(0));
    }

    @Override
    public void stop() {
        kMotorLeft.stopMotor();
    }

    @Override
    public void resetPosition() {
        kMotorLeft.setPosition(0.0);
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

        kMotorLeft.getConfigurator().apply(slot0);
    }

    @Override
    public void setMotionMagicConstraints(double maxVelocity, double maxAcceleration) {
        var motionMagic = motorConfiguration.MotionMagic;
        motionMagic.MotionMagicCruiseVelocity = metersToRotations(maxVelocity);
        motionMagic.MotionMagicAcceleration = metersToRotations(maxAcceleration);
        motionMagic.MotionMagicJerk = 10 * metersToRotations(maxAcceleration);

        kMotorLeft.getConfigurator().apply(motionMagic);
    }

    @Override
    public void setBrakeMode(boolean brake) {
        kMotorLeft.setNeutralMode(brake ? NeutralModeValue.Brake : NeutralModeValue.Coast);
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
