SPECK 어플 결제 서버입니다.

클라이언트 -> 가맹점 서버(SPECK 결제 서버) -> 아임포트 서버 -> PG 사 서버 -> 아임포트 서버 -> 가맹점 서버(SPECK 서버) 순으로 이루어집니다.
       
       
클라이언트가 결제요청을하면 SPECK 서버에서 요청파라미터를 통해 아임포트 서버에 결제 요청을 한후, 
결제 성공 또는 결제 실패 시 웹훅을 통해 결과를 리턴해주는 방식입니다.
