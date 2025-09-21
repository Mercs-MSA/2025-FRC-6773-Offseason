// // Copyright (c) FIRST and other WPILib contributors.
// // Open Source Software; you can modify and/or share it under the terms of
// // the WPILib BSD license file in the root directory of this project.

// package frc.robot.subsystems.elevator;

// import org.littletonrobotics.junction.Logger;

// import edu.wpi.first.math.MathUtil;
// import edu.wpi.first.math.controller.ElevatorFeedforward;
// import edu.wpi.first.math.controller.PIDController;
// import edu.wpi.first.math.trajectory.TrapezoidProfile;
// import edu.wpi.first.wpilibj.simulation.ElevatorSim;
// import frc.robot.subsystems.elevator.ElevatorConstants.ElevatorGains;
// import frc.robot.subsystems.elevator.ElevatorConstants.ElevatorHardware;
// // import frc.robot.subsystems.elevator.ElevatorConstants.SimulationConfiguration;

// public class ElevatorIOSim extends ElevatorIO {
//     private final double kLoopPeriodSec;

//     private final ElevatorSim m_elevatorSim;

//     private TrapezoidProfile kProfile;

//     private final PIDController kFeedback;

//     private ElevatorFeedforward kFeedforward;

//     private double appliedVoltage = 0.0;

//     private boolean feedbackNeedsReset = false;
//     private boolean closedLoopControl = false;

//     public ElevatorIOSim(ElevatorHardware hardware,
//     // SimulationConfiguration configuration,
//     ElevatorGains gains,
//     double minPositionMeters,
//     double maxPositionMeters,
//     double loopPeriodSec) {
        
//     }
// }