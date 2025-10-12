package frc.robot.subsystems.elevator;

public interface ElevatorIO {

    public class ElevatorIOInputs {
        public boolean isMotorConnected = false;

        public double positionMeters = 0.0;
        public double velocityMetersPerSec = 0.0;
        public double appliedVolts = 0.0;
        public double statorCurrentAmps = 0.0;
        public double supplyCurrentAmps = 0.0;
        public double temperatureCelsius = 0.0;
    }

    public void updateInputs(ElevatorIOInputs inputs);

    public void setVoltage(double volts);

    public void setPosition(double positionMeters);

    public void resetPosition();

    public void stop();

    public void setGains(double p, double i, double d, double v, double s, double g, double a);

    public void setMotionMagicConstraints(double maxVelocity, double maxAcceleration);

    public void setBrakeMode(boolean brake);
    
}
