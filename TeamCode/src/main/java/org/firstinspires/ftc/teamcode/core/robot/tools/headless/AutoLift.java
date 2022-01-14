package org.firstinspires.ftc.teamcode.core.robot.tools.headless;

import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.core.thread.EventThread;
import org.firstinspires.ftc.teamcode.core.thread.types.impl.TimedEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class AutoLift {

    // 5 1/4 inch from back of robot to rim
    MultipleTelemetry telemetry = new MultipleTelemetry();
    public enum Positions {
        INTAKING(0, 0.76D, false),
        SAFE(1375, 0.7D, false),
        TOP(2880, 0.3D, true),
        MIDDLE(1850, 0.3D, true),
        BOTTOM(1375, 0.25D, true),
        TSE(4500, 0.7D, true);

        public final double armPos;
        public final int motorPos;
        public final boolean dumper;
        
        Positions(int motorPos, double armPos, boolean dumper) {
            this.motorPos = motorPos;
            this.armPos = armPos;
            this.dumper = dumper;
        }
    }

    protected enum MovementStates { // switch this to a bool if you have time
        NONE,
        START,
        LIFT_MOVEMENT,
        SERVO_MOVEMENT
    }

    public final DcMotor liftMotor;
    protected final Servo armServo;
    protected final EventThread eventThread;
    protected Positions position = Positions.INTAKING;
    protected Positions lastPosition = position;
    protected MovementStates state = MovementStates.NONE;
    protected AutoGrabber grabber;
    // fix this later
    /**
     * @param eventThread local eventThread instance
     * @param map         local hardwareMap instance
     * @param grabber     grabber instance
     */
    public AutoLift(EventThread eventThread, @NonNull HardwareMap map, @Nullable AutoGrabber grabber) {
        liftMotor = map.get(DcMotor.class,"liftMotor");
        liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        liftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        liftMotor.setTargetPosition(liftMotor.getCurrentPosition());
        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftMotor.setPower(1);
        armServo = map.get(Servo.class,"armServo");
        this.eventThread = eventThread;
        this.grabber = grabber;
    }

    /**
     * @param eventThread local eventThread instance
     * @param map         local hardwareMap instance
     */
    public AutoLift(EventThread eventThread, HardwareMap map) {
        this(eventThread, map, null);
    }

    public void setPosition(@NonNull Positions position) {
        this.position = position;
    }

    public Positions getPosition() {
        return position;
    }

    public void blockingSetPosition(@NonNull Positions position) {
        setPosition(position);
        //insert some funny code that blocks until it has moved to position, will be very useful for finian burkard auto
    }

    private boolean dumpWaiting = true;

    public void update() {
        if (position != lastPosition) state = MovementStates.START;
        switch (state) {
            case START:
                if (lastPosition == null) {

                }
                armServo.setPosition(0.7D);
                liftMotor.setTargetPosition(position.motorPos);
                state = MovementStates.LIFT_MOVEMENT;
                break;
            case LIFT_MOVEMENT:
                final double motorPos = liftMotor.getCurrentPosition();
                if (motorPos >= position.motorPos - 10 && motorPos <= position.motorPos + 10) {
                    armServo.setPosition(position.armPos);
                    if (!position.dumper) state = MovementStates.NONE;
                    else {
                        dumpWaiting = true;
                        if (position == Positions.TSE && Objects.nonNull(grabber)) {
                            grabber.open();
                        }
                        eventThread.addEvent(new TimedEvent(() -> dumpWaiting = false, position == Positions.TSE ? 800 : 1400));
                        state = MovementStates.SERVO_MOVEMENT;
                    }
                }
                break;
            case SERVO_MOVEMENT:
                if (!dumpWaiting) {
                    position = Positions.INTAKING;
                    lastPosition = null;
                    return;
                }
                break;
        }
        lastPosition = position;
    }
}
