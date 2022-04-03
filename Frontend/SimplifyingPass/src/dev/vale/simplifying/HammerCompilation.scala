package dev.vale.simplifying

import dev.vale.highertyping.{ICompileErrorA, ProgramA}
import dev.vale.finalast.ProgramH
import dev.vale.options.GlobalOptions
import dev.vale.parsing.FailedParse
import dev.vale.parsing.ast.FileP
import dev.vale.postparsing.{ICompileErrorS, ProgramS}
import dev.vale.typing.{Hinputs, ICompileErrorT, TypingPassCompilation, TypingPassCompilationOptions}
import dev.vale.{FileCoordinateMap, IPackageResolver, PackageCoordinate, PackageCoordinateMap, Result, vassertSome, vcurious}
import dev.vale.{FileCoordinateMap, IPackageResolver, PackageCoordinate, PackageCoordinateMap, Profiler, Result, vassertSome, vcurious, vimpl}
import dev.vale.highertyping.ICompileErrorA
import dev.vale.postparsing.ICompileErrorS
import dev.vale.typing.ICompileErrorT

import scala.collection.immutable.List

case class HammerCompilationOptions(
  debugOut: (=> String) => Unit = (x => {
    println("##: " + x)
  }),
  globalOptions: GlobalOptions = GlobalOptions()
) { val hash = runtime.ScalaRunTime._hashCode(this); override def hashCode(): Int = hash; override def equals(obj: Any): Boolean = vcurious(); }

class HammerCompilation(
  packagesToBuild: Vector[PackageCoordinate],
  packageToContentsResolver: IPackageResolver[Map[String, String]],
  options: HammerCompilationOptions = HammerCompilationOptions()) {
  var typingPassCompilation =
    new TypingPassCompilation(
      packagesToBuild,
      packageToContentsResolver,
      TypingPassCompilationOptions(
        options.globalOptions,
        options.debugOut))
  var hamutsCache: Option[ProgramH] = None
  var vonHammerCache: Option[VonHammer] = None

  def getVonHammer() = vassertSome(vonHammerCache)

  def interner = typingPassCompilation.interner

  def getCodeMap(): Result[FileCoordinateMap[String], FailedParse] = typingPassCompilation.getCodeMap()
  def getParseds(): Result[FileCoordinateMap[(FileP, Vector[(Int, Int)])], FailedParse] = typingPassCompilation.getParseds()
  def getVpstMap(): Result[FileCoordinateMap[String], FailedParse] = typingPassCompilation.getVpstMap()
  def getScoutput(): Result[FileCoordinateMap[ProgramS], ICompileErrorS] = typingPassCompilation.getScoutput()
  def getAstrouts(): Result[PackageCoordinateMap[ProgramA], ICompileErrorA] = typingPassCompilation.getAstrouts()
  def getCompilerOutputs(): Result[Hinputs, ICompileErrorT] = typingPassCompilation.getCompilerOutputs()
  def expectCompilerOutputs(): Hinputs = typingPassCompilation.expectCompilerOutputs()

  def getHamuts(): ProgramH = {
    hamutsCache match {
      case Some(hamuts) => hamuts
      case None => {
        val hammer = new Hammer(interner)
        val hamuts = hammer.translate(typingPassCompilation.expectCompilerOutputs())
        hamutsCache = Some(hamuts)
        vonHammerCache = Some(hammer.vonHammer)
        hamuts
      }
    }
  }
}