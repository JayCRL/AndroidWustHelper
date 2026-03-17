import Foundation
import SwiftUI
import Network

// MARK: - 网络错误类型枚举
enum NetworkError: LocalizedError {
    case noInternetConnection
    case timeout
    case serverUnavailable
    case invalidResponse
    case dataParsingError
    case authenticationFailed
    case serverError(Int, message: String?)
    case unknown(String)
    
    var errorDescription: String? {
        switch self {
        case .noInternetConnection:
            return "网络未连接，请检查WiFi或移动数据"
        case .timeout:
            return "请求超时，请稍后重试"
        case .serverUnavailable:
            return "服务器暂时不可用，请稍后重试"
        case .invalidResponse:
            return "服务器返回无效响应"
        case .dataParsingError:
            return "数据解析失败，请稍后重试"
        case .authenticationFailed:
            return "登录失效，请重新登录"
        case .serverError(let code, let message):
            if let msg = message {
                return msg
            }
            return "服务器错误（状态码：\(code)）"
        case .unknown(let message):
            return message
        }
    }
}

// MARK: - 网络请求结果
enum NetworkResult<T> {
    case success(T)
    case failure(NetworkError)
}

// MARK: - 统一网络请求处理器
class NetworkErrorHandler {
    
    // MARK: - 处理URLSession错误
    static func handleURLError(_ error: Error) -> NetworkError {
        if let urlError = error as? URLError {
            switch urlError.code {
            case .notConnectedToInternet, .networkConnectionLost:
                return .noInternetConnection
            case .timedOut:
                return .timeout
            case .cannotFindHost, .cannotConnectToHost:
                return .serverUnavailable
            case .userAuthenticationRequired, .userCancelledAuthentication:
                return .authenticationFailed
            default:
                return .unknown(urlError.localizedDescription)
            }
        } else {
            return .unknown(error.localizedDescription)
        }
    }
    
    // MARK: - 处理HTTP响应
    static func handleHTTPResponse(_ response: URLResponse?, data: Data? = nil) -> NetworkError? {
        guard let httpResponse = response as? HTTPURLResponse else {
            return .invalidResponse
        }
        
        switch httpResponse.statusCode {
        case 200...299:
            return nil // 成功
        case 401, 403:
            return .authenticationFailed
        case 404:
            return .serverUnavailable
        case 500...599:
            // 尝试解析返回体的message字段
            var message: String? = nil
            if let data = data {
                if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let msg = json["message"] as? String {
                    message = msg
                } else if let rawStr = String(data: data, encoding: .utf8),
                          let jsonData = rawStr.data(using: .utf8),
                          let json = try? JSONSerialization.jsonObject(with: jsonData) as? [String: Any],
                          let msg = json["message"] as? String {
                    message = msg
                }
            }
            return .serverError(httpResponse.statusCode, message: message)
        default:
            return .serverError(httpResponse.statusCode, message: nil)
        }
    }
    
    // MARK: - 打印请求信息
    static func printRequestInfo(_ request: URLRequest) {
        print("【NetworkErrorHandler 请求信息】")
        print("  URL: \(request.url?.absoluteString ?? "未知")")
        print("  方法: \(request.httpMethod ?? "未知")")
        print("  请求头:")
        if let headers = request.allHTTPHeaderFields, !headers.isEmpty {
            for (key, value) in headers.sorted(by: { $0.key < $1.key }) {
                // 敏感信息部分隐藏
                if key.lowercased() == "cookie" || key.lowercased() == "authorization" {
                    let valueStr = String(value)
                    if valueStr.count > 50 {
                        print("    \(key): \(String(valueStr.prefix(50)))...")
                    } else {
                        print("    \(key): \(valueStr)")
                    }
                } else {
                    print("    \(key): \(value)")
                }
            }
        } else {
            print("    (无)")
        }
        if let body = request.httpBody {
            if let bodyStr = String(data: body, encoding: .utf8) {
                print("  请求体: \(bodyStr)")
            } else {
                print("  请求体: \(body.count) 字节（二进制数据）")
            }
        } else {
            print("  请求体: (无)")
        }
    }
    
    // MARK: - 处理数据解析错误
    static func handleParsingError(_ error: Error, data: Data? = nil, request: URLRequest? = nil) -> NetworkError {
        if let request = request {
            printRequestInfo(request)
        }
        print("【NetworkErrorHandler 解析错误】错误信息：\(error.localizedDescription)")
        if let data = data, let rawDataStr = String(data: data, encoding: .utf8) {
            print("【NetworkErrorHandler 原始数据】：\(rawDataStr)")
            
            if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
               let message = json["message"] as? String {
                return .serverError(-1, message: message)
            }
        } else if let data = data {
            print("【NetworkErrorHandler 原始数据（无法解析为字符串）】：\(data.count) 字节")
            print("【NetworkErrorHandler 原始数据（十六进制）】：\(data.map { String(format: "%02x", $0) }.joined())")
        }
        return .dataParsingError
    }
    
    // MARK: - 统一网络请求方法（带重试机制）
    static func performRequest<T: Codable>(
        url: URL,
        method: HTTPMethod = .GET,
        headers: [String: String] = [:],
        body: Data? = nil,
        timeout: TimeInterval = 15,
        responseType: T.Type,
        maxRetries: Int = 1,
        retryDelay: TimeInterval = 3.0,
        completion: @escaping (NetworkResult<T>) -> Void
    ) {
        performRequestInternal(
            url: url,
            method: method,
            headers: headers,
            body: body,
            timeout: timeout,
            responseType: responseType,
            retryCount: 0,
            maxRetries: maxRetries,
            retryDelay: retryDelay,
            completion: completion
        )
    }
    
    // MARK: - 内部请求方法（支持重试）
    private static func performRequestInternal<T: Codable>(
        url: URL,
        method: HTTPMethod = .GET,
        headers: [String: String] = [:],
        body: Data? = nil,
        timeout: TimeInterval = 15,
        responseType: T.Type,
        retryCount: Int,
        maxRetries: Int,
        retryDelay: TimeInterval,
        completion: @escaping (NetworkResult<T>) -> Void
    ) {
        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        request.timeoutInterval = timeout
        
        // 添加请求头
        for (key, value) in headers {
            request.addValue(value, forHTTPHeaderField: key)
        }
        
        // 添加请求体
        if let body = body {
            request.httpBody = body
        }
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                // 处理网络错误
                if let error = error {
                    printRequestInfo(request)
                    print("【NetworkErrorHandler 网络错误】错误信息：\(error.localizedDescription)")
                    if let data = data, let rawDataStr = String(data: data, encoding: .utf8) {
                        print("【NetworkErrorHandler 原始数据】：\(rawDataStr)")
                    } else if let data = data {
                        print("【NetworkErrorHandler 原始数据（无法解析为字符串）】：\(data.count) 字节")
                        print("【NetworkErrorHandler 原始数据（十六进制）】：\(data.map { String(format: "%02x", $0) }.joined())")
                    }
                    let networkError = handleURLError(error)
                    completion(.failure(networkError))
                    return
                }
                
                // 处理HTTP响应错误（传入data以便解析message）
                if let httpError = handleHTTPResponse(response, data: data) {
                    printRequestInfo(request)
                    if let httpResponse = response as? HTTPURLResponse {
                        print("【NetworkErrorHandler HTTP错误】状态码：\(httpResponse.statusCode)")
                        
                        // 如果是502错误且还有重试次数，则重试
                        if httpResponse.statusCode == 502 && retryCount < maxRetries {
                            print("【NetworkErrorHandler】检测到502错误，等待\(retryDelay)秒后自动重试（第\(retryCount + 1)/\(maxRetries)次）...")
                            DispatchQueue.main.asyncAfter(deadline: .now() + retryDelay) {
                                performRequestInternal(
                                    url: url,
                                    method: method,
                                    headers: headers,
                                    body: body,
                                    timeout: timeout,
                                    responseType: responseType,
                                    retryCount: retryCount + 1,
                                    maxRetries: maxRetries,
                                    retryDelay: retryDelay,
                                    completion: completion
                                )
                            }
                            return
                        }
                    }
                    if let data = data, let rawDataStr = String(data: data, encoding: .utf8) {
                        print("【NetworkErrorHandler 原始数据】：\(rawDataStr)")
                    } else if let data = data {
                        print("【NetworkErrorHandler 原始数据（无法解析为字符串）】：\(data.count) 字节")
                        print("【NetworkErrorHandler 原始数据（十六进制）】：\(data.map { String(format: "%02x", $0) }.joined())")
                    }
                    completion(.failure(httpError))
                    return
                }
                
                // 检查数据
                guard let data = data, !data.isEmpty else {
                    printRequestInfo(request)
                    print("【NetworkErrorHandler 错误】无返回数据")
                    completion(.failure(.invalidResponse))
                    return
                }
                
                // 解析数据
                do {
                    let decoder = JSONDecoder()
                    let result = try decoder.decode(T.self, from: data)
                    if retryCount > 0 {
                        print("【NetworkErrorHandler】重试成功！")
                    }
                    completion(.success(result))
                } catch {
                    let parsingError = handleParsingError(error, data: data, request: request)
                    completion(.failure(parsingError))
                }
            }
        }
        
        task.resume()
    }
    
    // MARK: - 简化的GET请求
    static func get<T: Codable>(
        url: URL,
        headers: [String: String] = [:],
        responseType: T.Type,
        completion: @escaping (NetworkResult<T>) -> Void
    ) {
        performRequest(
            url: url,
            method: .GET,
            headers: headers,
            responseType: responseType,
            completion: completion
        )
    }
    
    // MARK: - 简化的POST请求
    static func post<T: Codable>(
        url: URL,
        headers: [String: String] = [:],
        body: Data? = nil,
        responseType: T.Type,
        completion: @escaping (NetworkResult<T>) -> Void
    ) {
        performRequest(
            url: url,
            method: .POST,
            headers: headers,
            body: body,
            responseType: responseType,
            completion: completion
        )
    }
}

// MARK: - HTTP方法枚举
enum HTTPMethod: String {
    case GET = "GET"
    case POST = "POST"
    case PUT = "PUT"
    case DELETE = "DELETE"
}

// MARK: - 错误信息显示扩展
extension NetworkError {
    var userFriendlyMessage: String {
        switch self {
        case .noInternetConnection:
            return "请检查网络连接后重试"
        case .timeout:
            return "网络请求超时，请重试"
        case .serverUnavailable:
            return "服务暂时不可用，请稍后重试"
        case .invalidResponse:
            return "服务器响应异常，请重试"
        case .dataParsingError:
            return "数据解析失败，请重试"
        case .authenticationFailed:
            return "登录已失效，请重新登录"
        case .serverError(let code, let message):
            if let msg = message {
                return msg
            }
            return "服务器错误(\(code))，请稍后重试"
        case .unknown(let message):
            return "网络错误：\(message)"
        }
    }
    
    var shouldRetry: Bool {
        switch self {
        case .noInternetConnection, .timeout, .serverUnavailable, .serverError:
            return true
        case .invalidResponse, .dataParsingError, .authenticationFailed, .unknown:
            return false
        }
    }
    
    // 获取HTTP状态码（如果有）
    var httpStatusCode: Int? {
        switch self {
        case .serverError(let code, _):
            return code
        default:
            return nil
        }
    }
    
    // 获取服务器返回的message（如果有）
    var serverMessage: String? {
        switch self {
        case .serverError(_, let message):
            return message
        default:
            return nil
        }
    }
}

// MARK: - 网络状态监控
class NetworkMonitor: ObservableObject {
    @Published var isConnected = true
    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "NetworkMonitor")
    
    init() {
        monitor.pathUpdateHandler = { [weak self] path in
            DispatchQueue.main.async {
                self?.isConnected = path.status == .satisfied
            }
        }
        monitor.start(queue: queue)
    }
    
    deinit {
        monitor.cancel()
    }
}
