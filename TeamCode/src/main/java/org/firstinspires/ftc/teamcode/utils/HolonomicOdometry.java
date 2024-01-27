package org.firstinspires.ftc.teamcode.utils;
import org.firstinspires.ftc.teamcode.utils.geometry.*;

import com.arcrobotics.ftclib.kinematics.Odometry;

import java.util.function.DoubleSupplier;

public class HolonomicOdometry {

    private double prevLeftEncoder, prevRightEncoder, prevHorizontalEncoder;
    private Rotation2d previousAngle;
    private double centerWheelOffset;
    protected Pose2d robotPose;

    public Pose2d getPose() {
        return robotPose;
    }

    /**
     * The trackwidth of the odometers
     */
    protected double m_trackWidth;

    // the suppliers
    DoubleSupplier m_left, m_right, m_horizontal, m_gyro;

    public HolonomicOdometry(DoubleSupplier leftEncoder, DoubleSupplier rightEncoder,
                             DoubleSupplier horizontalEncoder, DoubleSupplier gyroAngle, double trackWidth, double centerWheelOffset) {
        this(trackWidth, centerWheelOffset);
        m_left = leftEncoder;
        m_right = rightEncoder;
        m_horizontal = horizontalEncoder;
        m_gyro = gyroAngle;
    }

    public HolonomicOdometry(Pose2d initialPose, double trackwidth, double centerWheelOffset) {
        this.m_trackWidth = trackwidth;
        previousAngle = initialPose.getRotation();
        this.centerWheelOffset = centerWheelOffset;
    }

    public HolonomicOdometry(double trackwidth, double centerWheelOffset) {
        this(new Pose2d(), trackwidth, centerWheelOffset);
    }

    /**
     * This handles all the calculations for you.
     */
    public void updatePose() {
        update(m_left.getAsDouble(), m_right.getAsDouble(), m_horizontal.getAsDouble(), m_gyro.getAsDouble());
    }

    public void setPose(Pose2d pose) {
        previousAngle = pose.getRotation();
        robotPose = pose;

        prevLeftEncoder = 0;
        prevRightEncoder = 0;
        prevHorizontalEncoder = 0;
    }

    public void update(double leftEncoderPos, double rightEncoderPos, double horizontalEncoderPos,double gyroAngle) {
        double deltaLeftEncoder = leftEncoderPos - prevLeftEncoder;
        double deltaRightEncoder = rightEncoderPos - prevRightEncoder;
        double deltaHorizontalEncoder = horizontalEncoderPos - prevHorizontalEncoder;

        Rotation2d angle = Rotation2d.fromDegrees(gyroAngle);

        prevLeftEncoder = leftEncoderPos;
        prevRightEncoder = rightEncoderPos;
        prevHorizontalEncoder = horizontalEncoderPos;

        double dw = (angle.minus(previousAngle).getRadians());

        double dx = (deltaLeftEncoder + deltaRightEncoder) / 2;
        double dy = deltaHorizontalEncoder - (centerWheelOffset * dw);

        Twist2d twist2d = new Twist2d(dx, dy, dw);

        Pose2d newPose = robotPose.exp(twist2d);

        previousAngle = angle;

        robotPose = new Pose2d(newPose.getTranslation(), angle);
    }

}
