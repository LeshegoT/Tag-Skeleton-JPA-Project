package za.co.sbg.tag.skeleton.app.boundary;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@ApplicationPath("/")
@SecurityScheme(securitySchemeName = "Authorization", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")

public class RestApplication extends Application {
}
