# Anti-spoofing model

Đặt file model của bạn vào thư mục này.

Ten mac dinh project dang tim:

```text
best_model_quantized.onnx
```

Duong dan mac dinh trong `.env`:

```env
ANTI_SPOOF_MODEL_PATH=models/anti_spoofing/best_model_quantized.onnx
```

Model mac dinh nay can anh RGB 128x128, gia tri pixel 0..1 va output 2 logits:
`[real, spoof]`.

Không commit/share model nếu bạn không chắc license của file model.
