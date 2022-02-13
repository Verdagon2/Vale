package net.verdagon.vale.parser.patterns

import net.verdagon.vale.parser.ast.Patterns._

import net.verdagon.vale.parser._
import net.verdagon.vale.parser.ast.{DestructureP, LocalNameDeclarationP, TuplePT, NameOrRunePT, NameP, PatternPP}

import net.verdagon.vale.{Collector, vfail, vimpl}
import org.scalatest.{FunSuite, Matchers}

class CaptureAndDestructureTests extends FunSuite with Matchers with Collector with TestParseUtils {
  private def compile[T](code: String): PatternPP = {
    compile(x => new PatternParser().parsePattern(x), code)
  }

  test("Capture with destructure with type inside") {
    compile("a [a int, b bool]") shouldHave {
      case PatternPP(_,_,
          Some(LocalNameDeclarationP(NameP(_, "a"))),
          None,
          Some(
          DestructureP(_,
            Vector(
              capturedWithType("a", NameOrRunePT(NameP(_, "int"))),
              capturedWithType("b", NameOrRunePT(NameP(_, "bool")))))),
          None) =>
    }
  }
  test("capture with empty sequence type") {
    compile("a ()") shouldHave {
      case capturedWithType("a", TuplePT(_,Vector())) =>
    }
  }
  test("empty destructure") {
    compile(new PatternParser().parseDestructure(_),"[]") shouldHave { case Nil => }
  }
  test("capture with empty destructure") {
    // Needs the space between the braces, see https://github.com/ValeLang/Vale/issues/434
    compile("a [ ]") shouldHave {
      case PatternPP(_,_,Some(LocalNameDeclarationP(NameP(_, "a"))),None,Some(DestructureP(_,Vector())),None) =>
    }
  }
  test("Destructure with nested atom") {
    compile("a [b int]") shouldHave {
      case PatternPP(_,_,
          Some(LocalNameDeclarationP(NameP(_, "a"))),
          None,
          Some(
          DestructureP(_,
            Vector(capturedWithType("b", NameOrRunePT(NameP(_, "int")))))),
          None) =>
    }
  }
}
