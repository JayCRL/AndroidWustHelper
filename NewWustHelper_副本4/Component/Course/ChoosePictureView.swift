import SwiftUI
import PhotosUI
struct ChoosePictureView: View {
    @State var selectedItem: PhotosPickerItem? = nil
    @Binding var backgroundImagePath: String 
    @Binding var opacityNumber:Double
    @State var selectedBackgroundImage: Image? = nil
    @State var opacityValue: Double = 0.7  // 控制透明度的变量
    @State var selectedImageData: Data? = nil // 用于存储选择的图片数据
    @State var showAlert: Bool = false
    @State var showDeleteAlert: Bool = false  // 删除确认弹窗的状态
    @Binding var showSignal:Bool
    var onSave:(Int) -> Void
    // 在 ChoosePictureView 中替换 saveImageToDocuments 函数
    func handleSelectedItem() {
        guard let selectedItem else { return }
        Task {
            if let data = try? await selectedItem.loadTransferable(type: Data.self),
               let uiImage = UIImage(data: data) {
                // 保存图片到本地 Documents 目录
                let fileName = UUID().uuidString + ".jpg" // 用 UUID 生成一个唯一的文件名
                if let savedURL = await saveImageToDocuments(image: uiImage, imageName: fileName) {
                    // 将路径保存到 AppStorage 中
                    backgroundImagePath = savedURL.path
                    selectedBackgroundImage = Image(uiImage: uiImage)
                    // 在保存图片后，执行 onSave 操作
                    onSave(1)  // 现在确保在保存后才调用
                    print("save completed")
                }
            }
        }
    }

    // 将保存图片的过程改为 async 方法
    func saveImageToDocuments(image: UIImage, imageName: String) async -> URL? {
        // 1. 正确获取 Documents 目录（确保路径无误）
        guard let documentsURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            print("无法获取 Documents 目录")
            return nil
        }
        let imageURL = documentsURL.appendingPathComponent(imageName)
        print("准备保存到：\(imageURL.path)") // 打印目标路径

        // 2. 确保图片数据有效（jpegData 可能返回 nil）
        guard let imageData = image.jpegData(compressionQuality: 0.8) else { // 降低压缩质量，避免数据为 nil
            print("图片数据无效（jpegData 返回 nil）")
            return nil
        }
        // 3. 写入文件（捕获具体错误）
        do {
            try imageData.write(to: imageURL)
            print("文件保存成功：\(imageURL.path)")
            return imageURL
        } catch {
            print("文件写入失败：\(error.localizedDescription)") // 打印具体错误（如权限、磁盘满）
            return nil
        }
    }
    func deleteImageFromDocuments() {
        // 删除存储在 Documents 目录中的图片
        let fileManager = FileManager.default
        if !backgroundImagePath.isEmpty {
            let imageURL = URL(fileURLWithPath: backgroundImagePath)
            do {
                try fileManager.removeItem(at: imageURL)  // 删除图片文件
                backgroundImagePath = ""  // 清空保存的路径
                selectedImageData = nil  // 清空选中的图片数据
                selectedBackgroundImage = nil  // 清空显示的背景图
                onSave(1)  // 现在确保在保存后才调用
            } catch {
                print("删除图片失败: \(error.localizedDescription)")
            }
        }
    }
    
    var body: some View {
        NavigationView {
            VStack {
                // 选择图片并实时显示
                PhotosPicker(selection: $selectedItem, matching: .images, photoLibrary: .shared()) {
                    // 如果有选中的图片数据，显示它
                    if let selectedImageData, let uiImage = UIImage(data: selectedImageData) {
                        Image(uiImage: uiImage)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 300, height: 350)
                            .opacity(opacityValue)  // 设置图片透明度
                            .padding()
                    } else {
                        Text("选择图片")
                            .padding()
                    }
                }
                .onChange(of: selectedItem) { newItem in
                    // 在选择图片时更新 selectedImageData
                    Task {
                        if let selectedItem = newItem {
                            do {
                                // 从 PhotosPicker 中获取图像数据
                                let data = try await selectedItem.loadTransferable(type: Data.self)
                                selectedImageData = data
                            } catch {
                                print("无法加载图片数据")
                            }
                        }
                    }
                }

                // 显示当前透明度值
                HStack {
                    Text("当前透明度: \(String(format: "%.2f", opacityValue))")
                        .padding()
                    Spacer()
                }

                // 透明度调整条
                Slider(value: $opacityValue, in: 0...1, step: 0.05)
                    .padding()
                    .accentColor(.blue)

                Spacer()
            }
            .alert("确认保存?", isPresented: $showAlert) {
                Button("取消", role: .cancel) {
                }
                Button("确认", role: .destructive) {
                    // 只有在点击“保存”时才执行保存操作
                    if let selectedItem = selectedItem {
                        handleSelectedItem() // 执行保存到本地操作
                        print(backgroundImagePath)
                    }
                    opacityNumber=opacityValue
                    showAlert = false
                    showSignal=false
                }
            }
            .alert("确认删除?", isPresented: $showDeleteAlert) {
                Button("取消", role: .cancel) {                }
                Button("确认删除", role: .destructive) {
                    // 删除图片
                    selectedImageData=nil
                    deleteImageFromDocuments()  // 删除本地文件
                    showSignal=false
                }
            }
            .padding()
            .navigationBarItems(
                leading: Button("取消") {
                    // 取消操作
                    // 可以重置状态或关闭视图
                    showSignal=false
                },
                trailing: HStack {
                    Button(role: .destructive) {
                        showDeleteAlert = true  // 显示删除确认弹窗
                    } label: {
                        Image(systemName: "trash")
                    }
                    // 保存按钮
                    Button("保存") {
                        showAlert = true
                    }
                }
            )
        }.onAppear(){
            // 在初始化时检查 backgroundImagePath 是否有值，如果有值，则加载图片
            if !backgroundImagePath.isEmpty {
                let imageURL = URL(fileURLWithPath: backgroundImagePath)
                if let imageData = try? Data(contentsOf: imageURL) {
                    selectedImageData = imageData
                    opacityValue=opacityNumber
                }
                
            }
           
        }
    }
}

