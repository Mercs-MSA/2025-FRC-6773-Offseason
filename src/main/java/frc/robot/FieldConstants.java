package frc.robot;
import org.littletonrobotics.junction.AutoLog;
import org.littletonrobotics.junction.AutoLogOutput;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.robot.utils.debugging.LoggedTunableNumber;

public class FieldConstants {
    public static final Pose2d kReefCenter = new Pose2d(4.48249, Constants.kFieldWidthMeters / 2.0, Rotation2d.fromDegrees(0.0));
    public static final double kXNetLineMeters = 7.15;
    public static LoggedTunableNumber testPoseX = new LoggedTunableNumber("Field/TestPoseX", 3.0);
    public static LoggedTunableNumber testPoseY = new LoggedTunableNumber("Field/TestPoseY", 3.0);
    public static LoggedTunableNumber testPoseRotation = new LoggedTunableNumber("Field/TestPoseRotation", Rotation2d.k180deg.getDegrees());

    public static final Pose2d AL = new Pose2d(3.15, 4.2, Rotation2d.kZero);
    public static final Pose2d AR = new Pose2d(3.15, 3.87, Rotation2d.kZero);
    public static final Pose2d AM = new Pose2d(average(AL.getX(), AR.getX()), average(AL.getY(), AR.getY()), Rotation2d.kZero);

    public static final Pose2d BL = new Pose2d(4.063492298126221, 5.203369140625, Rotation2d.fromRadians(2.0943951023931953 + Math.PI));
    public static final Pose2d BM = new Pose2d(3.8979341983795166, 5.141284942626953, Rotation2d.fromRadians(2.0943951023931953 + Math.PI));
    public static final Pose2d BR = new Pose2d(3.7323758602142334, 5.058506011962891, Rotation2d.fromRadians(2.0943951023931953 + Math.PI));

    // === C poses ===
    public static final Pose2d CL = new Pose2d(5.305179119110107, 5.058506011962891, Rotation2d.fromRadians(1.0471975511965976 + Math.PI));
    @AutoLogOutput(key = "TESTPOSES/CM")
    public static final Pose2d CM = new Pose2d(5.139620780944824, 5.141284942626953, Rotation2d.fromRadians(1.0471975511965976 + Math.PI));
    @AutoLogOutput(key = "TESTPOSES/CR")
    public static final Pose2d CR = new Pose2d(4.994757175445557, 5.224063873291016, Rotation2d.fromRadians(1.0471975511965976 + Math.PI));

    // === D poses ===
    @AutoLogOutput(key = "TESTPOSES/DL")
    public static final Pose2d DL = new Pose2d(5.760464191436768, 3.816819190979004, Rotation2d.fromRadians(0.0 + Math.PI));
    @AutoLogOutput(key = "TESTPOSES/DM")
    public static final Pose2d DM = new Pose2d(5.760464191436768, 4.023766994476318, Rotation2d.fromRadians(0.0 + Math.PI));
    @AutoLogOutput(key = "TESTPOSES/DR")
    public static final Pose2d DR = new Pose2d(5.760464191436768, 4.168630599975586, Rotation2d.fromRadians(0.0 + Math.PI));

    // === E poses ===
    @AutoLogOutput(key = "TESTPOSES/EL")
    public static final Pose2d EL = new Pose2d(4.974062442779541, 2.8648595809936523, Rotation2d.fromRadians(-1.0471975511965976 + Math.PI));
    @AutoLogOutput(key = "TESTPOSES/EM")
    public static final Pose2d EM = new Pose2d(5.098231315612793, 2.926943778991699, Rotation2d.fromRadians(-1.0471975511965976 + Math.PI));
    @AutoLogOutput(key = "TESTPOSES/ER")
    public static final Pose2d ER = new Pose2d(5.222399711608887, 3.009722948074341, Rotation2d.fromRadians(-1.0471975511965976 + Math.PI));

    // === F poses ===
    @AutoLogOutput(key = "TESTPOSES/FL")
    public static final Pose2d FL = new Pose2d(3.7116811275482178, 2.989028215408325, Rotation2d.fromRadians(-2.0943951023931953 + Math.PI));
    @AutoLogOutput(key = "TESTPOSES/FM")
    public static final Pose2d FM = new Pose2d(3.8565444946289062, 2.9062490463256836, Rotation2d.fromRadians(-2.0943951023931953 + Math.PI));
    @AutoLogOutput(key = "TESTPOSES/FR")
    public static final Pose2d FR = new Pose2d(3.980713129043579, 2.8648595809936523, Rotation2d.fromRadians(-2.0943951023931953 + Math.PI));

    @AutoLogOutput(key = "TESTPOSES/B_IL")
    public static final Pose2d B_IL = new Pose2d(1.42, 7.21, Rotation2d.fromRadians(2.20));
    @AutoLogOutput(key = "TESTPOSES/B_IR")
    public static final Pose2d B_IR = new Pose2d(1.17, 1.02, Rotation2d.fromRadians(-2.20));
    @AutoLogOutput(key = "TESTPOSES/R_IL")
    public static final Pose2d R_IL = new Pose2d(16.08, 0.78, Rotation2d.fromRadians(2.20).plus(Rotation2d.k180deg));
    @AutoLogOutput(key = "TESTPOSES/R_IR")
    public static final Pose2d R_IR = new Pose2d(16.45, 7.01, Rotation2d.fromRadians(-2.20).plus(Rotation2d.k180deg));
    public static double average(double a, double b) {
        return (a + b) / 2.0;
    }
}