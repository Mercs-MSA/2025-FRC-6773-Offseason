// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.event.EventLoop;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

import edu.wpi.first.wpilibj2.command.button.Trigger;
// import frc.robot.subsystems.drive.Drive;
// import frc.robot.subsystems.drive.GyroIO;
// import frc.robot.subsystems.drive.GyroIOPigeon2;
// import frc.robot.subsystems.drive.ModuleIOKraken;
// import frc.robot.subsystems.drive.Module;
// import frc.robot.subsystems.drive.ModuleIO;
// import frc.robot.subsystems.drive.ModuleIOSim;
// import frc.robot.subsystems.drive.Drive.DriveState;
// import frc.robot.subsystems.drive.controllers.GoalPoseChooser;
// import frc.robot.subsystems.drive.controllers.GoalPoseChooser.SIDE;

import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.IntakeConstants;
import frc.robot.subsystems.intake.IntakePivotIO;
import frc.robot.subsystems.intake.IntakePivotIOSim;
import frc.robot.subsystems.intake.IntakePivotIOTalonFX;
import frc.robot.subsystems.intake.IntakeRollerIO;
import frc.robot.subsystems.intake.IntakeRollerIOSim;
import frc.robot.subsystems.intake.IntakeRollerIOTalonFX;
import frc.robot.subsystems.intake.Intake.IntakePivotGoal;
import frc.robot.TeleopCommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.Elevator.Elevator;
import frc.robot.subsystems.Elevator.ElevatorConstants;
import frc.robot.subsystems.Elevator.ElevatorIO;
import frc.robot.subsystems.Elevator.ElevatorIOSim;
import frc.robot.Constants;
import frc.robot.subsystems.Elevator.ElevatorIOTalonFX;
import frc.robot.subsystems.manipulator.Manipulator;
import frc.robot.subsystems.manipulator.ManipulatorConstants;
import frc.robot.subsystems.manipulator.ManipulatorIOTalonFX;
import frc.robot.subsystems.Elevator.Elevator.ElevatorGoal;
import frc.robot.utils.debugging.SysIDCharacterization;
import pabeles.concurrency.ConcurrencyOps.NewInstance;

public class RobotContainer {
    // Define subsystems
    private final Intake m_intake;    
    // Define other utility classes
    
    private LoggedDashboardChooser<Command> autoChooser;
    private final CommandXboxController driverController = new CommandXboxController(0);
    private final CommandXboxController operatorController = new CommandXboxController(1);
    private final Elevator m_Elevator;
    private final Manipulator m_Manipulator;


    /* TODO: Set to true before competition
     please */

    private final boolean useCompetitionBindings = true;

    // Anshul said to use this because he loves event loops
    private final EventLoop teleopLoop = new EventLoop();

    private final TeleopCommands teleopCommands;


    public RobotContainer() {
        switch (Constants.kCurrentMode) {
            case REAL:
            //    robotDrive = new Drive( new Module[] {
            //         new Module("FL", new ModuleIOKraken(kFrontLeftHardware )),
            //         new Module("FR", new ModuleIOKraken(kFrontRightHardware)),
            //         new Module("BL", new ModuleIOKraken(kBackLeftHardware  )),
            //         new Module("BR", new ModuleIOKraken(kBackRightHardware ))
            //     }, new GyroIOPigeon2());

                m_intake = new Intake(
                    new IntakePivotIOTalonFX(
                        IntakeConstants.kPivotMotorHardware,
                        IntakeConstants.kPivotMotorConfiguration,
                        IntakeConstants.kPivotGains,
                        IntakeConstants.kStatusSignalUpdateFrequencyHz),
                    new IntakeRollerIOTalonFX(
                        IntakeConstants.kRollerMotorHardware,
                        IntakeConstants.kRollerMotorConfiguration,
                        IntakeConstants.kStatusSignalUpdateFrequencyHz));
                m_Elevator = new Elevator(new ElevatorIOTalonFX(ElevatorConstants.kRoboElevatorHardware, ElevatorConstants.kMotorConfiguration, ElevatorConstants.kElevatorGains)); 
                m_Manipulator = new Manipulator(new ManipulatorIOTalonFX(ManipulatorConstants.kManipulatorHardware, ManipulatorConstants.kMotorConfiguration, ManipulatorConstants.kStatusSignalUpdateFrequencyHz));  
                break;
            case SIM:
            //    robotDrive = new Drive( new Module[] {
            //         new Module("FL", new ModuleIOSim()),
            //         new Module("FR", new ModuleIOSim()),
            //         new Module("BL", new ModuleIOSim()),
            //         new Module("BR", new ModuleIOSim())
            //     }, new GyroIO() {});

                m_intake = new Intake(
                    new IntakePivotIOSim(
                        0.02,
                        IntakeConstants.kPivotMotorHardware,
                        IntakeConstants.kPivotSimulationConfiguration,
                        IntakeConstants.kPivotGains),
                    new IntakeRollerIOSim(
                        0.02,
                        IntakeConstants.kRollerMotorHardware,
                        IntakeConstants.kIntakeRollerSimulationConfiguration));
                m_Elevator = new Elevator(new ElevatorIOSim(ElevatorConstants.kRoboElevatorHardware, ElevatorConstants.kSimulationConfiguration, ElevatorConstants.kElevatorGains, 0.0, 10.0, 1.0));
                m_Manipulator = new Manipulator(new ManipulatorIOTalonFX(ManipulatorConstants.kManipulatorHardware, ManipulatorConstants.kMotorConfiguration, ManipulatorConstants.kStatusSignalUpdateFrequencyHz));
                break;
            default:
            //    robotDrive = new Drive( new Module[] {
            //         new Module("FL", new ModuleIO() {}),
            //         new Module("FR", new ModuleIO() {}),
            //         new Module("BL", new ModuleIO() {}),
            //         new Module("BR", new ModuleIO() {})
            //     }, new GyroIO() {});

                m_intake = new Intake(new IntakePivotIO(){}, new IntakeRollerIO(){});
                m_Elevator = new Elevator(new ElevatorIOSim(null, null, null, 0, 0, 0));
                m_Manipulator = new Manipulator(null);
                break;
        }
        // Instantiate subsystems that don't care about mode, or are non-AdvantageKit enabled.
        // ex: LEDs = new LEDSubsystem();
        teleopCommands = new TeleopCommands(m_intake, m_Elevator, m_Manipulator);

        // robotDrive.setDefaultCommand(Commands.run(() -> robotDrive.setDriveState(DriveState.TELEOP), robotDrive));

        // Pass subsystems to classes that need them for configuration
        // robotDrive.acceptJoystickInputs(
        //     () -> - driverController.getLeftY(),
        //     () -> - driverController.getLeftX(),
        //     () -> - driverController.getRightX(),
        //     () -> driverController.getHID().getPOV());


        // Create any Dashboard choosers (LoggedDashboardChooser, etc)

        // Configure controls (drivebase suppliers, DriverStation triggers, Button and other Controller bindings)
        configureButtonBindings();
            
    }

    /* Commands to schedule on telop start-up */

    public Command getTeleopCommand() {
        return new SequentialCommandGroup(
           
        );
    }


    public Command getAutonomousCommand() {

        return autoChooser.get();
    }

    public void getAutonomousExit() {
    }


 private void configureStateTriggers() {
        /* Due to roborio start up times sometimes modules aren't reset properly, this accounts for that */
        // new Trigger(DriverStation::isEnabled)
        //     .onTrue(
        //         /* Do not require robot drive or it will deschedule auto */);
    }



    private Command rumbleCommandOperator() {
        return Commands.startEnd(
            () -> operatorController.getHID().setRumble(RumbleType.kBothRumble, 1.0), 
            () -> operatorController.getHID().setRumble(RumbleType.kBothRumble, 0.0));
    }

    private Command rumbleCommandDriver() {
        return Commands.startEnd(
            () -> driverController.getHID().setRumble(RumbleType.kBothRumble, 1.0), 
            () -> driverController.getHID().setRumble(RumbleType.kBothRumble, 0.0));
    }

    private void configureButtonBindings() {
        ArrayList<Trigger> positionButtons = new ArrayList<Trigger>();
        positionButtons.add(operatorController.y());
        positionButtons.add(operatorController.b());
        positionButtons.add(operatorController.a());
        positionButtons.add(operatorController.x());



        if (useCompetitionBindings) {

            driverController.a().onTrue(new InstantCommand(() -> m_Elevator.setGoal(ElevatorGoal.kStow)));
            driverController.b().onTrue(new InstantCommand(() -> m_Elevator.setGoal(ElevatorGoal.kL3Coral)));
            driverController.y().onTrue(new InstantCommand(() -> m_Elevator.setGoal(ElevatorGoal.kL4Coral)));
            // driverController.leftBumper().onTrue(new InstantCommand(() -> m_Manipulator.intake()));
            driverController.rightTrigger().onTrue(new InstantCommand(() -> m_Manipulator.outtake()));
            // // driverController.y().onTrue(Commands.runOnce(() -> robotDrive.resetGyro()));

            // driverController.leftTrigger()
            //     .whileTrue(teleopCommands.runPivotAndHoldCommand(IntakePivotGoal.kFloorPickup))
            //     .onFalse(teleopCommands.runPivotAndHoldCommand(IntakePivotGoal.kStow));

            driverController.leftTrigger().onTrue(teleopCommands.floorIntakeCommand())
                .onFalse(teleopCommands.stowCommand());
            

            
        } 


        else {
            driverController.x().onTrue(SysIDCharacterization.runElevatorSysIDTests((voltage) -> m_Elevator.setVoltage(voltage), m_Elevator));
            // driverController.x()
            //     .onTrue(robotDrive.setDriveStateCommand(DriveState.SYSID_CHARACTERIZATION).andThen(Commands.run(() -> 
            //         robotDrive.runMOICharacterization(20), robotDrive)))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // // driverController.x()
            // //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.TELEOP_SNIPER))
            // //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // // getPOV == -1 if nothing is pressed, so if it doesn't return that
            // // then pov control is being used as its being pressed
            // new Trigger(()-> driverController.getHID().getPOV() != -1)
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.POV_SNIPER))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // driverController.b()
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.LINEAR_TEST))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // driverController.a()
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_BARGE))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));
        }
    }

        


    public EventLoop getTeleopEventLoop() {
        return teleopLoop;
    }
}