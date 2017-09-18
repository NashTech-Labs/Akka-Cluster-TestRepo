package sample.cluster.simple

import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.ActorLogging
import akka.actor.Actor

import scala.concurrent.Await
import scala.util.Try
import scala.concurrent.duration._

class SimpleClusterListener extends Actor with ActorLogging {

  val system =context.system
  val cluster = Cluster(context.system)

  // subscribe to cluster changes, re-subscribe when restart 
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case _: MemberEvent => // ignore
  }


/*  Cluster(system).registerOnMemberRemoved {
    // exit JVM when ActorSystem has been terminated
    system.registerOnTermination(System.exit(0))
    // shut down ActorSystem
    system.terminate()

    // In case ActorSystem shutdown takes longer than 10 seconds,
    // exit the JVM forcefully anyway.
    // We must spawn a separate thread to not block current thread,
    // since that would have blocked the shutdown of the ActorSystem.
    new Thread {
      override def run(): Unit = {
        if (Try(Await.ready(system.whenTerminated, 10.seconds)).isFailure)
          System.exit(-1)
      }
    }.start()
  }*/
}