// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.event.EventLoop;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;


import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.Mode;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.subsystems.drive.GyroIOPigeon2;
import frc.robot.subsystems.drive.ModuleIOKraken;
import frc.robot.subsystems.drive.Module;
import frc.robot.subsystems.drive.ModuleIO;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.drive.controllers.GoalPoseChooser;
import frc.robot.subsystems.drive.controllers.GoalPoseChooser.SIDE;
import frc.robot.subsystems.drive.Drive.DriveState;
import frc.robot.subsystems.elevator.Elevator;
import frc.robot.subsystems.elevator.Elevator.ElevatorGoal;
import frc.robot.subsystems.elevator.Elevator.ElevatorState;
import frc.robot.subsystems.elevator.ElevatorConstants;
import frc.robot.subsystems.elevator.ElevatorIOSim;
import frc.robot.subsystems.elevator.ElevatorIOTalonFX;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.IntakeConstants;
import frc.robot.subsystems.intake.IntakePivotIO;
import frc.robot.subsystems.intake.IntakePivotIOSim;
import frc.robot.subsystems.intake.IntakePivotIOTalonFX;
import frc.robot.subsystems.intake.IntakeRollerIO;
import frc.robot.subsystems.intake.IntakeRollerIOSim;
import frc.robot.subsystems.intake.IntakeRollerIOTalonFX;
import frc.robot.subsystems.intake.Intake.IntakePivotGoal;
import frc.robot.subsystems.manipulator.Manipulator;
import frc.robot.subsystems.manipulator.ManipulatorConstants;
import frc.robot.subsystems.manipulator.ManipulatorIOTalonFX;
import frc.robot.subsystems.vision.CameraIO;
import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.vision.VisionConstants;
import frc.robot.subsystems.vision.VisionIOLimelight;
import frc.robot.utils.debugging.LoggedTunableNumber;
import static frc.robot.subsystems.drive.DriveConstants.*;
import java.util.ArrayList;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;

public class RobotContainer {
    // Define subsystems
    private final Drive robotDrive;
    private final Intake m_Intake;  
    private final Elevator m_Elevator;
    private final Manipulator m_Manipulator;  
    private final Vision m_Vision;

    
    private Trigger intakeCoralTrigger;
    private Trigger manipulatorCoralTrigger;

    
    // Define other utility classes
    
    private LoggedDashboardChooser<String> autoChooser;
    private LoggedDashboardChooser<Double> speedChooser;
    private LoggedDashboardChooser<NeutralModeValue> bChooser;
    
    private final CommandXboxController driverController = new CommandXboxController(0);
    private final CommandXboxController operatorController = new CommandXboxController(1);

    /* TODO: Set to true before competition
     please */

    private final boolean useCompetitionBindings = true;

    // Anshul said to use this because he loves event loops
    private final EventLoop teleopLoop = new EventLoop();
    private final AutonCommands autonCommands;
    private final TeleopCommands teleopCommands;

    private LoggedTunableNumber startPos = new LoggedTunableNumber("Auton/StartPos (0 = U, 1 = M, 2 = B)", 1);
    private LoggedTunableNumber startReefPos = new LoggedTunableNumber("Auton/StartReefPos (0 = Reef RUp, 1 = Reef R, 2 = Reef D)", 1);
    private LoggedTunableNumber sourcePref = new LoggedTunableNumber("Auton/SourcePref (0 = Source T, 1 = Source B)", 1);
    
    public Field2d goalPoseField = new Field2d();
    public Field2d currPoseField = new Field2d();

    public Pose2d getCurrPose() {
        return robotDrive.getPoseEstimate();
    }

    public RobotContainer() {
        
        // If using AdvantageKit, perform mode-specific instantiation of subsystems.
        switch (Constants.kCurrentMode) {
            case REAL:

                
                m_Elevator = new Elevator(new ElevatorIOTalonFX(ElevatorConstants.kRoboElevatorHardware, ElevatorConstants.kMotorConfiguration, ElevatorConstants.kElevatorGains)); 

                robotDrive = new Drive( new Module[] {
                    new Module("FL", new ModuleIOKraken(kFrontLeftHardware )),
                    new Module("FR", new ModuleIOKraken(kFrontRightHardware)),
                    new Module("BL", new ModuleIOKraken(kBackLeftHardware  )),
                    new Module("BR", new ModuleIOKraken(kBackRightHardware ))
                }, new GyroIOPigeon2(), null, m_Elevator);

                m_Vision = new Vision(new CameraIO[]{
                    new VisionIOLimelight(VisionConstants.camera0Name, () -> robotDrive.getRobotRotation()),
                    // new VisionIOLimelight(VisionConstants.camera1Name, () -> robotDrive.getRobotRotation()),
                });

                robotDrive.setVision(m_Vision);
                m_Intake = new Intake(
                    new IntakePivotIOTalonFX(
                        IntakeConstants.kPivotMotorHardware,
                        IntakeConstants.kPivotMotorConfiguration,
                        IntakeConstants.kPivotGains,
                        IntakeConstants.kStatusSignalUpdateFrequencyHz),
                    new IntakeRollerIOTalonFX(
                        IntakeConstants.kRollerMotorHardware,
                        IntakeConstants.kRollerMotorConfiguration,
                        IntakeConstants.kStatusSignalUpdateFrequencyHz));
                m_Manipulator = new Manipulator(new ManipulatorIOTalonFX(ManipulatorConstants.kManipulatorHardware, ManipulatorConstants.kMotorConfiguration, ManipulatorConstants.kStatusSignalUpdateFrequencyHz));  
                break;
            case SIM:
                m_Elevator = new Elevator(new ElevatorIOSim(ElevatorConstants.kRoboElevatorHardware, ElevatorConstants.kSimulationConfiguration, ElevatorConstants.kElevatorGains, 0.0, 10.0, 1.0));

               robotDrive = new Drive( new Module[] {
                    new Module("FL", new ModuleIOSim()),
                    new Module("FR", new ModuleIOSim()),
                    new Module("BL", new ModuleIOSim()),
                    new Module("BR", new ModuleIOSim())
                }, new GyroIO() {}, null, m_Elevator);

                m_Vision = new Vision(new CameraIO[]{
                    new VisionIOLimelight(VisionConstants.camera0Name, () -> robotDrive.getRobotRotation()),
                    // new VisionIOLimelight(VisionConstants.camera1Name, () -> robotDrive.getRobotRotation()),
                });
                robotDrive.setVision(m_Vision);

                m_Manipulator = new Manipulator(new ManipulatorIOTalonFX(ManipulatorConstants.kManipulatorHardware, ManipulatorConstants.kMotorConfiguration, ManipulatorConstants.kStatusSignalUpdateFrequencyHz));
                m_Intake = new Intake(
                    new IntakePivotIOSim(
                        0.02,
                        IntakeConstants.kPivotMotorHardware,
                        IntakeConstants.kPivotSimulationConfiguration,
                        IntakeConstants.kPivotGains),
                    new IntakeRollerIOSim(
                        0.02,
                        IntakeConstants.kRollerMotorHardware,
                        IntakeConstants.kIntakeRollerSimulationConfiguration));
                break;
            default:
                m_Elevator = new Elevator(new ElevatorIOSim(null, null, null, 0, 0, 0));

                robotDrive = new Drive( new Module[] {
                    new Module("FL", new ModuleIO() {}),
                    new Module("FR", new ModuleIO() {}),
                    new Module("BL", new ModuleIO() {}),
                    new Module("BR", new ModuleIO() {})
                }, new GyroIO() {}, null, m_Elevator);

                m_Vision = new Vision(new CameraIO[]{
                    new VisionIOLimelight(VisionConstants.camera0Name, () -> robotDrive.getRobotRotation()),
                });

                m_Intake = new Intake(new IntakePivotIO(){}, new IntakeRollerIO(){});
                m_Manipulator = new Manipulator(null);
                break;
        }
        autonCommands = new AutonCommands(robotDrive, m_Elevator, m_Intake, m_Manipulator);
        teleopCommands = new TeleopCommands(m_Elevator, m_Intake, m_Manipulator, driverController);

        // Instantiate subsystems that don't care about mode, or are non-AdvantageKit enabled.
        // ex: LEDs = new LEDSubsystem();
        createAutos();
        speedChooser = new LoggedDashboardChooser<Double>("Teleop/Acceleration Chooser");
        speedChooser.addDefaultOption("NORMAL", 1.0);
        speedChooser.addOption("SLOW", 0.7);
        
        bChooser = new LoggedDashboardChooser<NeutralModeValue>("Drive/BrakeModeChooser");
        bChooser.addDefaultOption("BRAKE", NeutralModeValue.Brake);
        bChooser.addOption("COAST", NeutralModeValue.Coast);


        robotDrive.setDefaultCommand(Commands.run(() -> robotDrive.setDriveState(DriveState.TELEOP), robotDrive));
        //m_Elevator.setDefaultCommand(Commands.run(() -> m_Elevator.setGoal(ElevatorGoal.kStow), m_Elevator));
        //m_Intake.setDefaultCommand(Commands.run(()-> m_Intake.setPivotGoal(IntakePivotGoal.kStow), m_Intake));

        // Pass subsystems to classes that need them for configuration
        robotDrive.acceptJoystickInputs(
            () -> - Math.copySign(driverController.getLeftY() * driverController.getLeftY(), driverController.getLeftY()) * speedChooser.get(),
            () -> - Math.copySign(driverController.getLeftX() * driverController.getLeftX(), driverController.getLeftX()) * speedChooser.get(),
            () -> driverController.getRightX() * speedChooser.get().doubleValue(),
            () -> driverController.getHID().getPOV());


        // Create any Dashboard choosers (LoggedDashboardChooser, etc)

        // Configure controls (drivebase suppliers, DriverStation triggers, Button and other Controller bindings)
        configureStateTriggers();
        configureButtonBindings();

        

        
    }

    /* Commands to schedule on telop start-up */
    public Command getTeleopCommand() {
        return new SequentialCommandGroup(
            robotDrive.setDriveStateCommand(DriveState.TELEOP)
        );
    }

    public void createAutos() {
        autoChooser = new LoggedDashboardChooser<String>("Autonomous/chooser");

        // Add autos to chooser
        autoChooser.addOption("LEFT", "LEFT");
        autoChooser.addOption("RIGHT", "RIGHT");
        autoChooser.addOption("CENTER", "CENTER");
        autoChooser.addOption("NOTHING", "NOTHING");


        SmartDashboard.putData("Autonomous/AutoChooser", autoChooser.getSendableChooser());
    }

    public Command getAutonomousCommand() {








        //autoChooser.addOption("3PieceTRREEF-Chicken-McNuggets", Commands.sequence(leftThreePiece));

        // autoCommand.addCommands(autonCommands.runAutonScoringSegment(ElevatorState.L4, "ST_TLREEF", SIDE.LEFT));

        // switch ((int)startPos.get()) {
        //     case 0: // U
        //         startCommandName += "STT_";
        //         break;
        //     case 1: // M
        //         startCommandName += "STM_";
        //         break;
        //     case 2: // B
        //         startCommandName += "STB_";
        //         break;
        //     default:
        //         startCommandName += "STM_";
        //         break;
        // }

        // switch ((int)startReefPos.get()) {
        //     case 0: // Reef RUp
        //         startCommandName += "TRREEF";
        //         break;
        //     case 1: // Reef R
        //         startCommandName += "RREEF";
        //         break;
        //     case 2: // Reef D
        //         startCommandName += "BRREEF";
        //         break;
        //     default:
        //         startCommandName += "RREEF";
        //         break;
        // }
        
        // autoCommand.addCommands(autonCommands.runAutonScoringSegment(ElevatorState.L4, startCommandName));
        // startCommandName = startCommandName.split("_")[1];

        // // switch ((int)sourcePref.get()) {
        // //     case 0: // Source T
        // //         startCommandName += "_ST";
        // //         break;
        // //     case 1: // Source B
        // //         startCommandName += "_SB";
        // //         break;
        // //     default:
        // //         startCommandName += "_ST";
        // //         break;
        // // }
        // // autoCommand.addCommands(
        // //     autonCommands.runAutonIntakeSegment(startCommandName)
        // // );

        // // startCommandName = startCommandName.split("_")[1];

        // // switch ((int)sourcePref.get()) {
        // //     case 0: // Source T
        // //         startCommandName += "_TLREEF";
        // //         break;
        // //     case 1: // Source B
        // //         startCommandName += "_BLREEF";
        // //         break;
        // //     default:
        // //         startCommandName += "_TRREEF";
        // //         break;
        // // }

        // // autoCommand.addCommands(autonCommands.runAutonScoringSegment(ElevatorState.L4, startCommandName));

        // // for (int i = 0 ; i < 10; i++) {
        // //     startCommandName = startCommandName.split("_")[1] + "_" + startCommandName.split("_")[0];
        // //     autoCommand.addCommands(autonCommands.followChoreoPath(startCommandName));
        // // }

        return autonCommands.getAutonomousChosen(autoChooser.get());
    }

    public void getAutonomousExit() {
        robotDrive.setDriveState(DriveState.STOP);
    }

    private void configureStateTriggers() {
        /* Due to roborio start up times sometimes modules aren't reset properly, this accounts for that */
        new Trigger(DriverStation::isEnabled)
            .onTrue(
                /* Do not require robot drive or it will deschedule auto */
                Commands.runOnce(() -> robotDrive.resetModulesEncoders()));
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

        intakeCoralTrigger = new Trigger(() -> m_Intake.getCoralDetected());
        manipulatorCoralTrigger = new Trigger(() -> m_Manipulator.getCoralDetected());
        
        ArrayList<Trigger> positionButtons = new ArrayList<Trigger>();
        positionButtons.add(operatorController.y());
        positionButtons.add(operatorController.b());
        positionButtons.add(operatorController.a());
        positionButtons.add(operatorController.x());




        if (useCompetitionBindings) {

            intakeCoralTrigger.onTrue(rumbleCommandDriver().withTimeout(0.5).alongWith(rumbleCommandOperator().withTimeout(0.5)));
            manipulatorCoralTrigger.onTrue(rumbleCommandDriver().withTimeout(0.125).alongWith(rumbleCommandOperator().withTimeout(0.125)).andThen(rumbleCommandDriver().withTimeout(0.125).alongWith(rumbleCommandOperator().withTimeout(0.125))));



            driverController.y().onTrue(Commands.runOnce(() -> robotDrive.resetGyro()));

            driverController.x()
                 .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_INTAKE).andThen(Commands.runOnce(() -> goalPoseField.setRobotPose(robotDrive.getGoalPose()))))
                 .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            operatorController.leftTrigger()
                 .onTrue(GoalPoseChooser.setSideCommand(SIDE.LEFT));
 
             operatorController.rightTrigger()
                 .onTrue(GoalPoseChooser.setSideCommand(SIDE.RIGHT));

            driverController.leftStick()
                //.onTrue(GoalPoseChooser.setSideCommand(SIDE.LEFT))
                .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_CORAL).andThen(Commands.runOnce(() -> goalPoseField.setRobotPose(robotDrive.getGoalPose()))))
                .onTrue(teleopCommands.elevatorUpCommand())
                .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            driverController.rightStick()
                //.onTrue(GoalPoseChooser.setSideCommand(SIDE.RIGHT))
                .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_CORAL).andThen(Commands.runOnce(() -> goalPoseField.setRobotPose(robotDrive.getGoalPose()))))
                .onTrue(teleopCommands.elevatorUpCommand())
                .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));
           
            driverController.rightTrigger().onTrue(new InstantCommand(() -> m_Manipulator.outtake()));
            

            driverController.leftTrigger().onTrue(teleopCommands.floorIntakeCommand())
                .whileFalse(teleopCommands.stowCommand());

            operatorController.y().onTrue(teleopCommands.setElevatorStateCommand(ElevatorState.L4));
            operatorController.b().onTrue(teleopCommands.setElevatorStateCommand(ElevatorState.L3));
            operatorController.a().onTrue(teleopCommands.setElevatorStateCommand(ElevatorState.L2));
            operatorController.x().onTrue(teleopCommands.setElevatorStateCommand(ElevatorState.STOW));

            operatorController.povUp().onTrue(teleopCommands.elevatorEdgeCommand());

            operatorController.rightBumper().onTrue(teleopCommands.elevatorUpCommand());

            driverController.leftBumper()
            .onTrue(teleopCommands.substationIntakeCommand())
            .whileFalse(teleopCommands.stowCommand());

            driverController.leftBumper()
            .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_INTAKE)
            .onlyWhile(() -> !robotDrive.atGoal()))
            .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

        } 

        else {
            driverController.y().onTrue(Commands.runOnce(() -> robotDrive.resetGyro()));

            driverController.b()
                .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.LINEAR_TEST))
                .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));
            
            
        }
    }

    public EventLoop getTeleopEventLoop() {
        return teleopLoop;
    }

    public void setIntakeBrakeMode(){
        if(m_Intake.getCoralDetected()){
            m_Intake.setBrakeMode(false);
        } else if(!m_Intake.getCoralDetected()){
            m_Intake.setBrakeMode(true);
        }
    }

    public void setDriveBrakeMode() {
        robotDrive.setBrakeMode(bChooser.get());
    }

    public void setGyroInit() {
        robotDrive.resetGyro();
    }
    
}