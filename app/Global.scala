import play.api.mvc._
import play.api._

object Global extends WithFilters(CORSFilter()) with GlobalSettings {

}



