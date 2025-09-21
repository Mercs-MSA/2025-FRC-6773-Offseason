package frc.robot.subsystems.elevator;


import org.littletonrobotics.junction.AutoLog;

public interface ElevatorIO {
    @AutoLog
    public static class ElevatorIOInputs {
        public boolean isMotorConnected = true;
        public double position = 0.0;
        public double velocity = 0.0;
        public double appliedVoltage = 0.0;
        public double supplyAmp = 0.0;
        public double statorAmp = 0.0;
        public double tempCelcius = 0.0;
        
        // position, velocity, voltage, supply, stator, temp
    }

    public default void updateInputs(ElevatorIOInputs inputs) {}

    public default void setVoltage(double volts) {}

    public default void setAmperage(double amps) {}

    public default void stop() {}

    public default void setGains(double p, double i, double d, double s, double g, double v, double a) {}

    public default void setMotionMagicConstraints(double maxVelocity, double maxAcceleration) {}

    public default void setBrakeMode(boolean brake) {}

    public default void resetPos() {}



}
