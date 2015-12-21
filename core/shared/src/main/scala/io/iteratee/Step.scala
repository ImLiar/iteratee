package io.iteratee

import cats.{ Applicative, Functor }

/**
 * Represents a pair of functions that can be used to reduce a [[Step]] to a value.
 *
 * Combining two "functions" into a single class allows us to save allocations.
 *
 * @tparam E The type of the input data
 * @tparam F The effect type constructor
 * @tparam A The type of the result calculated by the [[Iteratee]]
 * @tparam B The type of the result of the fold
 */
abstract class StepFolder[F[_], E, A, B] extends Serializable {
  def onCont(k: Input[E] => Iteratee[F, E, A]): B
  def onDone(value: A, remainder: Input[E]): B
}

abstract class MapContStepFolder[F[_]: Applicative, E, A](step: Step[F, E, A])
  extends StepFolder[F, E, A, Iteratee[F, E, A]] {
    final def onDone(value: A, remainder: Input[E]): Iteratee[F, E, A] = step.pointI
  }

/**
 * Represents the current state of an [[Iteratee]].
 *
 * An [[Iteratee]] has either already calculated a result ([[Step.done]]) or is waiting for more
 * data ([[Step.cont]]).
 *
 * @tparam E The type of the input data
 * @tparam F The effect type constructor
 * @tparam A The type of the result calculated by the [[Iteratee]]
 */
sealed abstract class Step[F[_], E, A] extends Serializable {
  /**
   * The [[Iteratee]]'s result.
   *
   * In some cases we know that an iteratee has been constructed in such a way that it must be in a
   * completed state, even though that's not tracked by the type system. This method provides
   * (unsafe) access to the result for use in these situations.
   */
  private[iteratee] def unsafeValue: A

  /**
   * Reduce this [[Step]] to a value using the given pair of functions.
   */
  def foldWith[B](folder: StepFolder[F, E, A, B]): B

  def isDone: Boolean

  /**
   * Create an [[Iteratee]] with this [[Step]] as its state.
   */
  final def pointI(implicit F: Applicative[F]): Iteratee[F, E, A] = Iteratee.iteratee(F.pure(this))

  def map[B](f: A => B)(implicit F: Functor[F]): Step[F, E, B]
}

final object Step {
  /**
   * Create an incomplete state that will use the given function to process the next input.
   */
  final def cont[F[_], E, A](k: Input[E] => Iteratee[F, E, A]): Step[F, E, A] = new Step[F, E, A] {
    private[iteratee] final def unsafeValue: A = Iteratee.diverge[A]
    final def isDone: Boolean = false
    final def foldWith[B](folder: StepFolder[F, E, A, B]): B = folder.onCont(k)
    final def map[B](f: A => B)(implicit F: Functor[F]): Step[F, E, B] = cont(in => k(in).map(f))
  }

  /**
   * Create a new completed state with the given result and leftover input.
   */
  final def done[F[_], E, A](value: A, remaining: Input[E]): Step[F, E, A] = new Step[F, E, A] {
    private[iteratee] final def unsafeValue: A = value
    final def isDone: Boolean = true
    final def foldWith[B](folder: StepFolder[F, E, A, B]): B = folder.onDone(value, remaining)
    final def map[B](f: A => B)(implicit F: Functor[F]): Step[F, E, B] = done(f(value), remaining)
  }
}
