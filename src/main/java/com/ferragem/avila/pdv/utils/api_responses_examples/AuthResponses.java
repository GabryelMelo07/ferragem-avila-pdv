package com.ferragem.avila.pdv.utils.api_responses_examples;

public class AuthResponses {
    
    public final static String LISTAR_USUARIOS = """
        [
            {
                "id": "e6976cf9-8d2a-43a8-af68-9c77bee5c6c3",
                "username": "admin",
                "email": "admin@ferragemavila.com.br",
                "nome": "João",
                "sobrenome": "Silva",
                "roles": [
                    "admin"
                ]
            }
        ]
    """;
    
    public final static String LOGIN_SUCESSO = """
        {
            "accessToken": "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJmNmQzNDg3Yi04NDFiLTQzNDYtOTQwYi0xODA1ZGRjYzY0YjciLCJub21lIjoiQWRtaW4gSXN0cmF0b3IiLCJleHAiOjE3MzUzNDk0MDAsImlhdCI6MTczNTI2MzAwMCwic2NvcGUiOiJBRE1JTiJ9.nGrmEneV3Nil_g_a4ixbb-uaW67k9c0QhGoBnmMvsYmmEkG_O9IymljxEqvmx7M2fYP2DgVTlOEmdKNJ_rXFqtK05ZiXFMiCo-36uzG1Xm3EUOsJRCStnbrh1BblopH56WuBTBuw9t6gje99H7Fjbd6jrsi0buizSxBsbxwPoMHbULGhO2XlcoyN55gmfMvjVIshzr5soT3KhJnP1W8te8g1wgyPU6Dq8cqHYjp2YQizjK0Q5xhMw9t24gwV1tbGdYevpybVmfRjiWyDp6qm7yBvanTQ8MPNzI0_q1fiff8iZ4yQym7sI6whahyMCyPgT-QjYq_iAwM5CbluV52Chg",
            "refreshToken": "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJmNmQzNDg3Yi04NDFiLTQzNDYtOTQwYi0xODA1ZGRjYzY0YjciLCJleHAiOjE3MzY1NTkwMDAsImlhdCI6MTczNTI2MzAwMCwic2NvcGUiOiJBRE1JTiJ9.yzi8jdiqlNg2PlFh_4KIRRufFcZCzZqjV_Yxu20F3aE_XJimckA0_qheak22OwyHdFmkcf6vrolPxB9FcITB5saqvtLUOWAfdXVh52z2ngCkcCH8s0ac1_pGVYnYerzKx2jTgIIwqBcrp4cfIl1YkknmwggS6Q99AM6q4AKTuKOgDlw-RNGjRD8hImUC0J0rddJpwPMMvWRFJoa57BG6KytYrLn7Ythup02MeEMujY8So9l5b3h6HZj_dVg_SWaEuEt18Z96LNr8nK7IS3C80Jn49RnJt1e5Zbh_icXyLobEf1bCiiVoOrnud6CrzBzLAIkcQmdJJwcwstNLCnMUqg"
        }
    """;
    
    public final static String LOGIN_FALHA = """
        {
            "timestamp": "2024-12-26T22:34:51.08314",
            "status": "UNAUTHORIZED",
            "error": "Nome de usuário ou senha inválidos."
        }
    """;
    
    public final static String RESET_PASSWORD_USER_NOT_FOUND = """
        {
            "timestamp": "2024-12-26T22:46:28.3192063",
            "status": "NOT_FOUND",
            "error": "Usuário não encontrado"
        }
    """;
    
}
