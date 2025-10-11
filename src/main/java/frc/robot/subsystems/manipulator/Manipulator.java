package frc.robot.subsystems.manipulator;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.Trigger;

public class Manipulator extends SubsystemBase {

  enum IntakeState {
    INTAKING,
    OUTTAKING,
    IDLE,
  }

  private final ManipulatorIO kHardware;
  private final ManipulatorIOInputsAutoLogged kInputs = new ManipulatorIOInputsAutoLogged();

  public static final double kRollerIntakeVoltage = -4.0;
  public static final double kRollerOuttakeVoltage = -6.0;

  private IntakeState mIntakeState = IntakeState.IDLE;

  private Trigger mIntakeTrigger = new Trigger(this::getCoralDetected);

  public Manipulator(ManipulatorIO manipulatorIO) {
    kHardware = manipulatorIO;

    mIntakeTrigger.onTrue(new InstantCommand(() -> {
      // System.out.println("INTAKE COMMAND");
      if (mIntakeState == IntakeState.INTAKING) {
        stop();
      }
      
    }));
    mIntakeTrigger.onFalse(new InstantCommand(() -> {
      // System.out.println("OUTTAKE COMMAND");
      if (mIntakeState == IntakeState.OUTTAKING) {
        // System.out.println("STOP FROM TRIGGER");
        stop();
      }
    }));
  }

  @Override
  public void periodic() {
    kHardware.updateInputs(kInputs);
    Logger.processInputs("Manipulator/Inputs", kInputs);

  }

  public void stop() {
    mIntakeState = IntakeState.IDLE;
    kHardware.stop();
  }

  public void intake() {
    if (getCoralDetected()) {
      // Coral already in, do nothing or stop
      stop();
      return;
    }
  
    mIntakeState = IntakeState.INTAKING;
    kHardware.setVoltage(kRollerIntakeVoltage);
  }

  public void outtake() {
    mIntakeState = IntakeState.OUTTAKING;
    kHardware.setVoltage(kRollerOuttakeVoltage);
  }

  

  public boolean getCoralDetected() {
    return kInputs.beambreakBroken;
  }
}