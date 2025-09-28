package frc.robot.subsystems.Elevator;

import java.lang.Cloneable;
import java.lang.Override;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.inputs.LoggableInputs;

public class ElevatorIOInputsAutoLogged extends ElevatorIO.ElevatorIOInputs implements LoggableInputs, Cloneable {
  @Override
  public void toLog(LogTable table) {
    table.put("IsMotorConnected", isMotorConnected);
    table.put("PositionMeters", positionMeters);
    table.put("VelocityMetersPerSec", velocityMetersPerSec);
    table.put("appliedVolts", appliedVolts);
    table.put("SupplyCurrentAmps", supplyCurrentAmps);
    table.put("StatorCurrentAmps", statorCurrentAmps);
    table.put("TemperatureCelsius", temperatureCelsius);
  }

  @Override
  public void fromLog(LogTable table) {
    isMotorConnected = table.get("IsMotorConnected", isMotorConnected);
    positionMeters = table.get("PositionMeters", positionMeters);
    velocityMetersPerSec = table.get("VelocityMetersPerSec", velocityMetersPerSec);
    appliedVolts = table.get("appliedVolts", appliedVolts);
    supplyCurrentAmps = table.get("SupplyCurrentAmps", supplyCurrentAmps);
    statorCurrentAmps = table.get("StatorCurrentAmps", statorCurrentAmps);
    temperatureCelsius = table.get("TemperatureCelsius", temperatureCelsius);
  }

  public ElevatorIOInputsAutoLogged clone() {
    ElevatorIOInputsAutoLogged copy = new ElevatorIOInputsAutoLogged();
    copy.isMotorConnected = this.isMotorConnected;
    copy.positionMeters = this.positionMeters;
    copy.velocityMetersPerSec = this.velocityMetersPerSec;
    copy.appliedVolts = this.appliedVolts;
    copy.supplyCurrentAmps = this.supplyCurrentAmps;
    copy.statorCurrentAmps = this.statorCurrentAmps;
    copy.temperatureCelsius = this.temperatureCelsius;
    return copy;
  }
}
