package Z1;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class MathActorDivide extends AbstractBehavior<MathActor.MathCommandDivide> {

    private int operationCount;

    public MathActorDivide(ActorContext<MathActor.MathCommandDivide> context) {
        super(context);
        operationCount = 0;
    }

    public static Behavior<MathActor.MathCommandDivide> create() {
        return Behaviors.setup(MathActorDivide::new);
    }

    @Override
    public Receive<MathActor.MathCommandDivide> createReceive() {
        return newReceiveBuilder()
                .onMessage(MathActor.MathCommandDivide.class, this::onMathCommandDivide)
                .build();
    }

    private Behavior<MathActor.MathCommandDivide> onMathCommandDivide(MathActor.MathCommandDivide mathCommandDivide) {
        //        System.out.println("ActorDivide: received command: divide");
        int result = mathCommandDivide.firstNumber / mathCommandDivide.secondNumber;
//        System.out.println("ActorDivide: divide result = " + result);
//        System.out.println("ActorDivide: sending response");
        operationCount +=1;
        System.out.println("ActorDivide: I made " + operationCount + " operations");
        mathCommandDivide.replyTo.tell(new MathActor.MathCommandResult(result));
        return this;
    }
}
