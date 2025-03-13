# 우아한 티켓팅

## 프로젝트 소개

특정 시간에 트래픽이 몰리는 티켓팅 시스템을 구현해보고자 하였습니다.  
병목이 발생할 수 있는 구간에 다양한 구현 방식을 적용하고 실험하며 최적의 방식을 찾습니다.  
최종적으로는 다양한 구현 방식 중 가장 높은 안정성과 처리량을 가지는 기술을 선택하였습니다.

추가적으로 기존 티켓팅 시스템에서 느꼈던 불편점을 개선해보고자 하는 시도를 하였습니다.

## 핵심 기능
### 티켓팅 기능
1. 짧은 시간에 좌석을 선점하기 위해 몰리는 트래픽에서의 동시성을 제어합니다.
2. 더 나은 사용자 경험을 위해 실시간 좌석 선점 상황을 표시합니다. 
   - "이미 예약된 좌석입니다" 문제로 인해 티켓팅이 실패하는 부정적 경험을 개선합니다.  
### 대기열 기능 
- 새로고침해도 순서가 유지되는 대기열 기능을 구현합니다.
  - 다양한 이유로 발생하는 일시적인 대기열 페이지 이탈 시에도 대기열을 일정 시간 유지할 수 있도록 합니다.

## 팀원 소개
<table>
    <tr align="center">
        <td><B>Backend</B></td>
        <td><B>Backend</B></td>
        <td><B>Backend</B></td>
        <td><B>Backend</B></td>
    </tr>
    <tr align="center">
        <td><a href="https://github.com/seminchoi">최세민</a></td>
        <td><a href="https://github.com/mirageoasis">김현우</a></td>
        <td><a href="https://github.com/hseong3243">박혜성</a></td>
        <td><a href="https://github.com/lass9436">이영민</a></td>
    </tr>
    <tr align="center">
        <td>
            <img src="https://github.com/seminchoi.png?size=100">
        </td>
        <td>
            <img src="https://github.com/mirageoasis.png" width="100">
        </td>
        <td>
            <img src="https://github.com/hseong3243.png?size=130">
        </td>
        <td>
            <img src="https://github.com/lass9436.png?size=100">
        </td>
    </tr>
    <tr>
        <td>-인프라 구성</br>-티켓팅 구현</br>-부하 테스트</td>
        <td>-티켓팅 기능 구현</br>-모니터링 환경 및</br>-부하 테스트 환경 구성</td>
        <td>-대기열 구현</br>-인증 구현</br>-부하 테스트 환경 구성</br>-부하 테스트</td>
        <td>-대기열 기능 구현</br>-화면 구현</td>
    </tr>
</table>

## 기술 스택

<div> 
  <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white">
  <br/>

  <img src="https://img.shields.io/badge/locust-3ECC5F?style=for-the-badge&logo=locust&logoColor=white">
  <img src="https://img.shields.io/badge/aws-232F3E?style=for-the-badge&logo=amazonwebservices&logoColor=white">
  <img src="https://img.shields.io/badge/ec2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white">
  <br/>
</div>

# 기여한 부분

## 1. 인프라 아키텍처 설계 및 구성

![인프라 구성 excalidraw](https://github.com/user-attachments/assets/06d872ef-eabe-4747-87a8-063ff0fa0e88)
**구성 의도**
- 티켓팅 서버와 대기열 서버를 분리하고 ALB를 이용한 부하 분산
- Public subnet과 Private subnet을 분리하여 보안 강화

**문제점**
- 모든 서버가 동일한 가용영역에 배치되어 고가용성이 보장되지 않음
- ALB를 Public subnet에 배치했기 때문에 스프링 서버를 Public subnet에 배치할 이유가 없음
- 위의 이유들로 ALB의 효과를 보지 못함

프로젝트는 종료되었지만 위 문제점을 인식하여 다음과 같은 개선안을 그려보았습니다.

### 개선안
<img width="892" alt="image" src="https://github.com/user-attachments/assets/4502acc8-f2cf-4c7d-84a0-ce1efbcf9499" />

**설명**
- 각 서버를 서로 다른 가용 영역에 배치하고 추가적인 Scale Out으로 가용성을 높였습니다.
- Redis Sentinel과 RDS Multi-AZ 구성을 통해 단일 장애 지점을 방지합니다.
- 클라이언트는 ALB를 통해 서버에 요청할 수 있으므로 ALB와 NAT를 제외한 모든 서비스를 Private subnet에 배치하였습니다.


## 2. 동시성을 제어하는 티켓팅 기능 구현
![image](https://github.com/user-attachments/assets/95d704fe-ac3c-4e69-acc7-80cb4b09c694)
티켓팅은 위와 같이 좌석에 대한 경합이 일어나는 상황에 초점을 맞춰 구현하였습니다.
위 그림은 아래 플로우를 표현합니다.
1. 갑과 을이 좌석 목록을 조회하고 A 좌석이 비어있음을 확인합니다.
2. 갑이 A좌석 예매를 위한 트랜잭션을 시작하고 A 좌석이 예매 가능한 상태인 것을 확인합니다. 
3. 을 또한 A좌석 예매를 위한 트랜잭션을 시작하고 A 좌석이 예매 가능한 상태인 것을 확인합니다.
4. 갑은 성공적으로 A 좌석 예매를 성공합니다.
5. 을 또한 A 좌석 예매에 성공하며 더블 부킹(데이터 부정합) 문제가 발생합니다.

따라서 그림의 1번과 2번 지점에 락을 사용하여 동시성을 제어합니다.  
락은 다음과 같은 방법들을 구현하고 비교해보았습니다.
- 비관락 (MySQL 배타락) 
- Version 정보를 이용한 낙관락 (JPA)
- Redis Pub/Sub락
- Redis Spin락

각 방식을 구현하기 위해 인터페이스로 추상화하고, 각각의 기능 구성에 필요한 구현체를 Configuration 클래스에서 일괄적으로 빈으로 등록할 수 있도록 하였습니다.  
또한, 동시성 테스트를 통해 기능에 대한 검증까지 마쳤습니다. 

## 3. 부하 테스트 및 성능 비교
구현한 티켓팅 기능에 대하여 부하테스트 및 성능 비교를 진행하였습니다.
부하 테스트는 아래와 같은 환경에서 진행했습니다.
- 테스트 도구: Locust (Distribute Mode)
  - 구성: Master 1개(EC2 t3.small), Slave 10개(EC2 t3.small) 사용
  - 가상 유저 2500 명
- 서버 구성
  - 티켓팅 서버: EC2 t3.small
  - Redis 서버: EC2 t3.small
  - 테스트 데이터베이스: RDS t3.micro
  - 테스트 데이터 구성: 좌석 갯수 500개 (다양한 개수로 실험해 보았으나 너무 많은 좌석은 경합을 줄인다고 판단 되었음)

최초에는 개인 PC에서 Locust를 실행하여 부하 테스트를 해보려 했으나, 단일 PC 리소스 제약으로 인해 Locust와 스크립트를 담은 커스텀 AMI를 만들고 여러대 복제하여 큰 부하를 줄 수 있는 환경을 구성했습니다.

**테스트 시나리오**  
1. 사용자가 좌석 목록을 조회합니다.
2. 좌석 목록 중 하나를 랜덤으로 선택하여 예매를 시도합니다.

**테스트 결과**

<img width="827" alt="image" src="https://github.com/user-attachments/assets/77d97765-27a2-4052-9adb-397ca9111823" />

테스트를 수행하기 전, 레디스를 활용한 pub/sub락의 성능이 가장 좋을 것이라고 예상했습니다.  
하지만 결과는 정 반대로 나타났습니다.
```
DB 비관락 >= 논리적 낙관락(JPA) >= Redis Spin락 > Redis Pub/Sub 락
```
이러한 결과를 분석하여 다음과 같은 인사이트를 얻었습니다.
- 레디스를 활용한 분산락으로 성능 상의 이점을 기대하긴 어렵습니다. 따라서 단일 DB 시스템에서는 적합하지 않은 접근입니다.


- 좌석은 한 번 선점되면 이 후로는 경쟁이 일어나지 않습니다. 이렇게 경합이 크게 일어나지 않는 상황에서는 Spin락에서의 Spin이 거의 일어나지 않으므로 이벤트 발행 및 구독으로 인한 Pub/Sub 락의 비용이 더 큰 것으로 추정됩니다.   
Pub/Sub 락을 효과적으로 활용할 수 있는 상황은 **선착순 쿠폰 이벤트**처럼 동일한 자원에 대한 경쟁이 지속적으로 일어나는 상황이라고 추측할 수 있었습니다. 


- MySQL은 MVCC를 사용하기 때문에 비관락을 사용하더라도 단순 조회에 제약을 걸지 않습니다. 그렇기 때문에 재시도를 하지 않는 낙관락과 비교해도 성능이 밀리지 않습니다. 

또한 부하 테스트를 하기에는 단순한 비즈니스 로직과 가벼운 데이터 구성(=데이터베이스 부하 적음)이었다고 생각되어 아쉬움이 남았습니다.

## 팀원 기여

### 1. 유지 가능한 대기열 시스템 구현
대기열 시스템은 사용자가 페이지에서 실수로 이탈하더라도 유지되어 사용자 경험을 높일 수 있도록 설계했습니다.

![대기열 시스템 구성](https://github.com/user-attachments/assets/4a9b35a2-1c39-47d2-af63-a23b3e649a21)
대기열 시스템은 은행 창구의 대기번호표 시스템을 모티브로 구현하여 다음과 같이 동작하도록 설계하였습니다.

1. 대기 번호 발급기는 새로 온 사용자에게 대기 번호를 발급합니다. 
2. 입장 번호 표시기는 작업 공간의 여유를 확인하고 입장할 수 있는 가장 큰 번호를 호출합니다. 
3. 2번에서 호출한 번호보다 작은 번호표를 가진 사용자들은 대기 공간에서 나가고, 작업 공간의 세션을 얻습니다. 
4. 티켓팅을 완료하거나 시간이 만료된 사용자들의 세션을 삭제합니다.
5. 위 작업이 반복됩니다.

대기번호표 시스템 방식으로 구현하였기 때문에 단순한 수식으로 대기번호를 쉽게 계산할 수 있습니다.
```
입장번호 카운트 - 대기번호 카운트
```

실제 구현을 위한 자료구조는 다음과 같이 구성하였습니다.
- 대기번호 생성기: String
- 대기 공간 (큐): Sorted Set
- 입장번호 생성기: String
- 작업 공간: Set

### 2. 좌석 예매 여부 실시간 알림 (SSE)
<img src="https://github.com/user-attachments/assets/87a1ed80-2a65-4836-a0be-f75dc9fccdcf" width="444" height="507"/>  

"이미 예약된 좌석입니다" 문제로 인해 티켓팅을 실패하는 부정적 경험을 줄이기 위해 좌석 예매 여부를 실시간으로 알리는 기능을 SSE로 구현해보았습니다.

하지만 SSE 기능을 포함하면 서비스가 불가능할 정도로 부하가 심했기 때문에 해당 기능은 실제로 적용하기엔 어렵다고 판단했습니다.

## 기타 - 애자일하게 협업하기

<img width="361" alt="image" src="https://github.com/user-attachments/assets/19cea060-11da-4303-a04e-8a50786ddc2a" />

Github Projects, Issue를 활용해 해야하는 작업을 백로그로 정의하며 협업하였습니다.
요구사항은 가능한 작은 단위로 분리하여 우선순위을 정하고 시간을 추정하여 백로그로 정의했습니다.
이슈는 PR merge 이후 close 되도록 하였으며 각 PR은 최소 한 명 이상의 Code Review 및 Approve가 있어야 merge할 수 있도록 하였습니다.

작업을 작은 단위로 분리하고, 서로의 작업을 리뷰하면서 직접 구현한 기능이 아니더라도 서로가 구현하는 기능을 이해할 수 있었으며 작업 진행 상황이 잘 공유된다는 장점이 있었습니다.


### 전체 시나리오 시연

https://github.com/user-attachments/assets/eb29c948-4a1c-41fd-a7b5-b6f4d167969c
